package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service.getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    var totalList = mutableListOf<User>()
    repos.forEachIndexed { index, repo ->
        val tempList = service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        totalList = (totalList + tempList).aggregate().toMutableList()
        updateResults(totalList, index == repos.lastIndex)
    }
}
