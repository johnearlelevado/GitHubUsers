package to.tawk.githubusers.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadState.NotLoading
import androidx.paging.LoadState.Loading
import androidx.paging.LoadStates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import kotlin.Error
import androidx.paging.PagingDataAdapter
import androidx.paging.RemoteMediator
import androidx.paging.PagingSource
import androidx.paging.LoadType.REFRESH
import androidx.paging.LoadType

object MergedLoadStates {
    /**
     * Converts the raw [CombinedLoadStates] [Flow] from [PagingDataAdapter.loadStateFlow] into a new
     * [Flow] of [CombinedLoadStates] that track [CombinedLoadStates.mediator] states as they are
     * synchronously applied in the UI. Any [Loading] state triggered by [RemoteMediator] will only
     * transition back to [NotLoading] after the fetched items have been synchronously shown in UI by a
     * successful [PagingSource] load of type [REFRESH].
     *
     * Note: This class assumes that the [RemoteMediator] implementation always invalidates
     * [PagingSource] on a successful fetch, even if no data was modified (which Room does by default).
     * Using this class without this guarantee can cause [LoadState] to get indefinitely stuck as
     * [Loading] in cases where invalidation doesn't happen because the fetched network data represents
     * exactly what is already cached in DB.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun Flow<CombinedLoadStates>.asMergedLoadStates(): Flow<LoadStates> {
        val syncRemoteState = LoadStatesMerger()
        return scan(syncRemoteState.toLoadStates()) { _, combinedLoadStates ->
            syncRemoteState.updateFromCombinedLoadStates(combinedLoadStates)
            syncRemoteState.toLoadStates()
        }
    }
}

/**
 * Track the combined [LoadState] of [RemoteMediator] and [PagingSource], so that each load type
 * is only set to [NotLoading] when [RemoteMediator] load is applied on presenter-side.
 */
private class LoadStatesMerger {
    var refresh: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var prepend: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var append: LoadState = NotLoading(endOfPaginationReached = false)
        private set
    var refreshState: MergedState = MergedState.NOT_LOADING
        private set
    var prependState: MergedState = MergedState.NOT_LOADING
        private set
    var appendState: MergedState = MergedState.NOT_LOADING
        private set

    fun toLoadStates() = LoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append
    )

    /**
     * For every new emission of [CombinedLoadStates] from the original [Flow], update the
     * [MergedState] of each [LoadType] and compute the new [LoadState].
     */
    fun updateFromCombinedLoadStates(combinedLoadStates: CombinedLoadStates) {
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.refresh,
            remoteState = combinedLoadStates.mediator?.refresh,
            currentMergedState = refreshState,
        ).also {
            refresh = it.first
            refreshState = it.second
        }
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.prepend,
            remoteState = combinedLoadStates.mediator?.prepend,
            currentMergedState = prependState,
        ).also {
            prepend = it.first
            prependState = it.second
        }
        computeNextLoadStateAndMergedState(
            sourceRefreshState = combinedLoadStates.source.refresh,
            sourceState = combinedLoadStates.source.append,
            remoteState = combinedLoadStates.mediator?.append,
            currentMergedState = appendState,
        ).also {
            append = it.first
            appendState = it.second
        }
    }

    /**
     * Compute which [LoadState] and [MergedState] to transition, given the previous and current
     * state for a particular [LoadType].
     */
    private fun computeNextLoadStateAndMergedState(
        sourceRefreshState: LoadState,
        sourceState: LoadState,
        remoteState: LoadState?,
        currentMergedState: MergedState,
    ): Pair<LoadState, MergedState> {
        if (remoteState == null) return sourceState to MergedState.NOT_LOADING

        return when (currentMergedState) {
            MergedState.NOT_LOADING -> when (remoteState) {
                is Loading -> Loading to MergedState.REMOTE_STARTED
                is Error -> remoteState to MergedState.REMOTE_ERROR
                else -> NotLoading(remoteState.endOfPaginationReached) to MergedState.NOT_LOADING
            }
            MergedState.REMOTE_STARTED -> when {
                remoteState is Error -> remoteState to MergedState.REMOTE_ERROR
                sourceRefreshState is Loading -> Loading to MergedState.SOURCE_LOADING
                else -> Loading to MergedState.REMOTE_STARTED
            }
            MergedState.REMOTE_ERROR -> when (remoteState) {
                is Error -> remoteState to MergedState.REMOTE_ERROR
                else -> Loading to MergedState.REMOTE_STARTED
            }
            MergedState.SOURCE_LOADING -> when {
                sourceRefreshState is Error -> sourceRefreshState to MergedState.SOURCE_ERROR
                remoteState is Error -> remoteState to MergedState.REMOTE_ERROR
                sourceRefreshState is NotLoading -> {
                    NotLoading(remoteState.endOfPaginationReached) to MergedState.NOT_LOADING
                }
                else -> Loading to MergedState.SOURCE_LOADING
            }
            MergedState.SOURCE_ERROR -> when (sourceRefreshState) {
                is Error -> sourceRefreshState to MergedState.SOURCE_ERROR
                else -> sourceRefreshState to MergedState.SOURCE_LOADING
            }
        }
    }
}

/**
 * State machine used to compute [LoadState] values in [LoadStatesMerger].
 *
 * This allows [LoadStatesMerger] to track whether to block transitioning to [NotLoading] from the
 * [Loading] state if it was triggered by [RemoteMediator], until [PagingSource] invalidates and
 * completes [REFRESH].
 */
private enum class MergedState {
    /**
     * Idle state; defer to remote state for endOfPaginationReached.
     */
    NOT_LOADING,

    /**
     * Remote load triggered; start listening for source refresh.
     */
    REMOTE_STARTED,

    /**
     * Waiting for remote in error state to get retried
     */
    REMOTE_ERROR,

    /**
     * Source refresh triggered by remote invalidation, once this completes we can be sure
     * the next generation was loaded.
     */
    SOURCE_LOADING,

    /**
     *  Remote load completed, but waiting for source refresh in error state to get retried.
     */
    SOURCE_ERROR,
}
