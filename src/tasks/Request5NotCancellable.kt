package tasks

import contributors.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service.getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    return repos.map { repo ->
        GlobalScope.async {
            log("Starting loading for ${repo.name}")
            delay(3000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }.awaitAll().flatten().aggregate()
}