[bintrayImage]: https://api.bintray.com/packages/breadmoirai/maven/breadbot-framework/images/download.svg
[bintrayLink]: https://bintray.com/breadmoirai/maven/BreadBotFramework/_latestVersion
[contributorsImage]: https://img.shields.io/github/contributors/BreadMoirai/BreadBotFramework.svg
[contributorsLink]: https://github.com/BreadMoirai/BreadBotFramework/graphs/contributors
[issuesImage]: https://img.shields.io/github/issues-raw/BreadMoirai/BreadBotFramework.svg
[issuesLink]: https://github.com/BreadMoirai/BreadBotFramework/issues
[discordWidget]: https://discordapp.com/api/guilds/284822192821108736/widget.png
[discordInvite]: https://discord.gg/yAMdGU9
[wikiImage]: https://img.shields.io/badge/wiki-here-orange.svg
[wikiLink]: https://github.com/BreadMoirai/BreadBotFramework/wiki
[docsImage]: https://img.shields.io/badge/docs-here-yellow.svg
[docsLink]: https://breadmoirai.github.io/BreadBotFramework/

[ ![bintrayImage][] ][bintrayLink] 
[ ![contributorsImage][] ][contributorsLink]
[ ![issuesImage][] ][issuesLink]
[ ![discordWidget][] ][discordInvite]
[ ![wikiImage][]][wikiLink]
[ ![docsImage][]][docsLink]

# BreadBotFramework
This is a framework for Discord Bots

### Features
 - CommandModules
 - idk just look at the examples
##### Planned Features
 - Being able to do things with CommandModules instead of just having access to them.
 - Being able to link sent messages with Response objects
 - logs

This framework is built ontop of [JDA](https://github.com/DV8FromTheWorld/JDA)
## Download
You can check the [releases](https://github.com/BreadMoirai/BreadBotFramework/releases) tab for jars. This is also distributed via [bintray][bintrayLink].

### Adding as Dependency
[![bintrayImage][]][bintrayLink] 

When using the snippets below replace the **VERSION** key with the version shown above.

#### Gradle
```groovy
repositories {
  jcenter()
}

dependencies {
  compile 'com.github.breadmoirai:breadbot-framework:VERSION'
}
```

#### Maven
```xml
<repository>
  <id>jcenter</id>
  <name>jcenter</name>
  <url>http://jcenter.bintray.com/</url>
</repository>

<dependency>
  <groupId>com.github.breadmoirai</groupId>
  <artifactId>breadbot-framework</artifactId>
  <version>VERSION</version>
  <type>pom</type>
</dependency>
```

### Javadoc
Web access to the javadocs is provided [here][docsLink]. Please note that this is a work in progress and it is likely something you would like documented is not documented. Please open an issue via Github [Issues](https://github.com/BreadMoirai/BreadBotFramework/issues) or contact me directly through my [Discord][discordInvite].

# [Wiki](https://github.com/BreadMoirai/BreadBotFramework/wiki)
See the wiki linked above for usage and examples.
