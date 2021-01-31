package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service.getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val bufferedChannel = Channel<List<User>>(50)
    coroutineScope {
        // send result
        launch {
            repos.map { repo ->
                launch {
                    val tempList = service.getRepoContributors(req.org, repo.name)
                        .also { logUsers(repo, it) }
                        .bodyList()
                    bufferedChannel.send(tempList)
                }
            }.joinAll()
            bufferedChannel.close()
        }
        // receive result
        launch {
            var totalList = emptyList<User>()
            repeat(repos.size) {
                val list = bufferedChannel.receive()
                totalList = (totalList + list).aggregate()
                updateResults(totalList, it == repos.lastIndex)
            }
        }
    }
}
