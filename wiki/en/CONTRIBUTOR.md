**[English](CONTRIBUTOR.md)** | [简体中文](../zh-cn/CONTRIBUTOR.md)

# Contribute guide

This guide will show you how to contribute to **AabResGuard**. Please ask for an [issue](https://github.com/bytedance/AabResGuard/issues) or [pull request](https://github.com/bytedance/AabResGuard/pulls).
Take a few minutes to read this guide before.

## Contributing
We are always very happy to have contributions, whether for typo fix, bug fix or big new features. Please do not ever hesitate to ask a question or send a pull request.

## [#Code of Conduct](CODE_OF_CONDUCT.md)
Please make sure to read and observe our **[Code of Conduct](CODE_OF_CONDUCT.md)** .

## GitHub workflow
All work on **AabResGuard** happens directly on GitHub. Both core team members and external contributors send pull requests which go through the same review process.

We use the `develop` branch as our development branch, and this code is an unstable branch. Each version will create a `release` branch (such as `release/0.1.1`) as a stable release branch.
Each time a new version is released, it will be merged into the corresponding branch and the corresponding `tag` will be applied.

Here are the workflow for contributors:

- Fork to your own.
- Clone fork to local repository.
- Create a new branch and work on it.
- Keep your branch in sync.
- Commit your changes (make sure your commit message concise).
- Push your commits to your forked repository.
- Create a pull request.

Please follow the pull request template. Please make sure the PR has a corresponding issue.

After creating a PR, one or more reviewers will be assigned to the pull request. The reviewers will review the code.

Before merging a PR, squash any fix review feedback, typo, merged, and rebased sorts of commits. The final commit message should be clear and concise.

## Open an issue / PR
### Where to Find Known Issues
We will be using GitHub Issues for our public bugs. We will keep a close eye on this and try to make it clear when we have an internal fix in progress. Before filing a new issue, try to make sure your problem doesn't already exist.

### Reporting New Issues
The best way to get your bug fixed is to provide a reduced test case. Please provide a public repository with a runnable example.
