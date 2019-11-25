[English](../en/CONTRIBUTOR.md) | **[简体中文](../zh-cn/CONTRIBUTOR.md)**

# 贡献指南

这篇指南会指导你如何为 **AabResGuard** 贡献一份自己的力量，请在你要提 [issue](https://github.com/bytedance/AabResGuard/issues) 或者 [pull request](https://github.com/bytedance/AabResGuard/pulls) 
之前花几分钟来阅读一遍这篇指南。

## 贡献
我们随时都欢迎任何贡献，无论是简单的错别字修正，BUG 修复还是增加新功能。请踊跃提出问题或发起 PR。我们同样重视文档以及与其它开源项目的整合，欢迎在这方面做出贡献。

## [#行为准则](../en/CODE_OF_CONDUCT.md)
我们有一份[行为准则](../en/CODE_OF_CONDUCT.md)，希望所有的贡献者都能遵守，请花时间阅读一遍全文以确保你能明白哪些是可以做的，哪些是不可以做的。

## 研发流程
我们所有的工作都会放在 GitHub 上。不管是核心团队的成员还是外部贡献者的 pull request 都需要经过同样流程的 review。

我们使用 `develop` 分支作为我们的开发分支，这代码它是不稳定的分支。每个版本都会创建一个 `release` 分支（如 `release/0.1`） 作为稳定的发布分支。
每发布一个新版本都会将其合并到对应的分支并打上对应的 `tag`。

下面是开源贡献者常用的工作流（workflow）：

- 将仓库 fork 到自己的 GitHub 下
- 将 fork 后的仓库 clone 到本地
- 创建新的分支，在新的分支上进行开发操作（请确保对应的变更都有测试用例或 demo 进行验证）
- 保持分支与远程 master 分支一致（通过 fetch 和 rebase 操作）
- 在本地提交变更（注意 commit log 保持简练、规范），注意提交的 email 需要和 GitHub 的 email 保持一致
- 将提交 push 到 fork 的仓库下
- 创建一个 pull request (PR)

提交 PR 的时候请参考 [PR 模板](../en/PULL_REQUEST_TEMPLATE.md)。在进行较大的变更的时候请确保 PR 有一个对应的 Issue。


在合并 PR 的时候，请把多余的提交记录都 squash 成一个。最终的提交信息需要保证简练、规范。

## 提交 bug
### 查找已知的 Issues
我们使用 GitHub Issues 来管理项目 bug。 我们将密切关注已知 bug，并尽快修复。 在提交新问题之前，请尝试确保您的问题尚不存在。

### 提交新的 Issues
请按照 Issues Template 的指示来提交新的 Issues。
