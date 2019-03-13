import groovy.json.JsonSlurper
import groovy.xml.XmlUtil

class DoesAlibabaFollowTheirOwnGuidelines {

    static final String SEARCH_URL = 'https://api.github.com/search/repositories?q=org%3Aalibaba+language%3Ajava&order=desc&type=Repositories'

    static final String MAVEN_PLUGIN_XML_NODE = '''
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-pmd-plugin</artifactId>
  <version>3.8</version>
  <configuration>
    <sourceEncoding>${project.build.sourceEncoding}</sourceEncoding>
    <targetJdk>1.8</targetJdk>
    <printFailingErrors>true</printFailingErrors>
    <rulesets>
      <ruleset>rulesets/java/ali-comment.xml</ruleset>
      <ruleset>rulesets/java/ali-concurrent.xml</ruleset>
      <ruleset>rulesets/java/ali-constant.xml</ruleset>
      <ruleset>rulesets/java/ali-exception.xml</ruleset>
      <ruleset>rulesets/java/ali-flowcontrol.xml</ruleset>
      <ruleset>rulesets/java/ali-naming.xml</ruleset>
      <ruleset>rulesets/java/ali-oop.xml</ruleset>
      <ruleset>rulesets/java/ali-orm.xml</ruleset>
      <ruleset>rulesets/java/ali-other.xml</ruleset>
      <ruleset>rulesets/java/ali-set.xml</ruleset>
    </rulesets>
  </configuration>
  <executions>
    <execution>
      <phase>verify</phase>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
  <dependencies>
    <dependency>
      <groupId>com.alibaba.p3c</groupId>
      <artifactId>p3c-pmd</artifactId>
      <version>1.3.6</version>
    </dependency>
  </dependencies>
</plugin>
'''

    static void main(String[] args) {
        List<String> javaRepoNames = getAlibabaJavaRepos()

        println("I'm going to clone the following repositories: ${javaRepoNames.join(' ')}")

        javaRepoNames.each { cloneOne(cloneBaseDirectory(), it) }

        Map<String, File> mavenRepos = findMavenRepos(cloneBaseDirectory(), javaRepoNames)

        Map<String, String> results = mavenRepos.entrySet().collectEntries { [it.key, checkOne(it.value)] }

        printMarkdown(results)
    }

    static void printMarkdown(Map<String, String> map) {
        println '''
| 项目名 | 项目运行结果 | 备注 | 
|---|---|---|'''
        println(map.entrySet().collect { "|${it.key}|${it.value}||" }.join('\n'))
    }

    static Map<String, File> findMavenRepos(File cloneBaseDir, List<String> repoNames) {
        def result = [:]
        repoNames.each { repoName ->
            if (new File(cloneBaseDir, "${repoName}/pom.xml").isFile()) {
                result[repoName] = new File(cloneBaseDir, repoName)
            } else if (!isAndroidProject(new File(cloneBaseDir, repoName))) {
                new File(cloneBaseDir, repoName).listFiles().each { subDir ->
                    if (new File(subDir, 'pom.xml').isFile()) {
                        result["${repoName}-${subDir.name}"] = subDir
                    }
                }
            }
        }
        return result
    }

    static boolean isAndroidProject(File repoDir) {
        return new File(repoDir, 'README.md').exists() && new File(repoDir, 'README.md').text.contains('Android')
    }

    static String checkOne(File repoDir) {
        runInheritIO(repoDir, 'git', 'reset', 'HEAD', '--hard')

        if (new File(repoDir, "pom.xml").isFile()) {
            return mavenCheck(repoDir)
        } else {
            return "Not a maven repo"
        }
    }

    static String mavenCheck(File repoDir) {
        println("Checking ${repoDir}")

        applyPmdMavenPlugin(repoDir)

        String output = runMvnPmdCheck(repoDir)

        return extractResult(output)
    }

    static String extractResult(String output) {
        if (output ==~ /(?s).*You have (\d+) PMD.*/) {
            def matcher = output =~ /You have (\d+) PMD/
            return "${matcher[0][1]} PMD violations"
        } else if (output.contains('BUILD FAILURE')) {
            return "build failure"
        } else if (output.contains('BUILD SUCCESS')) {
            return "build successful"
        } else {
            return 'unknown'
        }
    }

    static String runMvnPmdCheck(File repoDir) {
        if (System.getProperty('minimumPriority')) {
            return runInheritIO(repoDir, determineMvnCmd(repoDir), 'clean', 'install', '-DskipTests', "-DminimumPriority=${System.getProperty('minimumPriority')}")
        } else {
            return runInheritIO(repoDir, determineMvnCmd(repoDir), 'clean', 'install', '-DskipTests')
        }
    }

    static String determineMvnCmd(File repoDir) {
        if (new File(repoDir, 'mvnw.cmd').isFile() && System.getProperty('os.name').contains('Windows')) {
            return 'mvnw.cmd'
        } else if (new File(repoDir, 'mvnw').isFile() && !System.getProperty('os.name').contains('Windows')) {
            return './mvnw'
        } else {
            return 'mvn'
        }
    }

    static void applyPmdMavenPlugin(File repoDir) {
        String pomXmlText = new File(repoDir, 'pom.xml').text
        if (pomXmlText.contains('maven-pmd-plugin')) {
            println("maven-pmd-plugin already applied for ${repoDir}")
        } else {
            def pomXml = new XmlParser().parseText(pomXmlText)
            assert pomXml.build.size() == 1: "pom.xml has no <build> tag!"
            if (pomXml.build[0].plugins.size() == 0) {
                def pluginsNode = new XmlParser().parseText("<plugins>${MAVEN_PLUGIN_XML_NODE}</plugins>")
                pomXml.build[0].children().add(0, pluginsNode)
            } else {
                def pluginNode = new XmlParser().parseText(MAVEN_PLUGIN_XML_NODE)
                pomXml.build[0].plugins[0].children().add(0, pluginNode)
            }

            new File(repoDir, 'pom.xml').text = XmlUtil.serialize(pomXml)
        }
    }

    static File cloneBaseDirectory() {
        File ret = new File('alibaba')

        if (!ret.exists()) {
            ret.mkdir()
        }
        return ret
    }

    static List<String> getAlibabaJavaRepos() {
        String json = SEARCH_URL.toURL().getText()
        return new JsonSlurper().parseText(json).items.collect { it.name }.findAll { !it.contains('Android') } + ['p3c']
    }

    static void cloneOne(File cloneBaseDir, String name) {
        File location = new File(cloneBaseDir, name)
        if (new File(location, '.git').exists()) {
            println("${name} exists, skip.")
            return
        } else {
            location.mkdir()
        }
        runInheritIO('git', 'clone', '--depth', '1', "https://github.com/alibaba/${name}.git", location.absolutePath)
    }

    static String runInheritIO(String... args) {
        return runInheritIO(new File('.'), args)
    }

    static String runInheritIO(File workingDir, String... args) {
        Process process = new ProcessBuilder().directory(workingDir).command(args).start()

        StringBuffer sb = new StringBuffer()

        Thread.start {
            connectStream(process.inputStream, System.out, sb)
        }
        Thread.start {
            connectStream(process.errorStream, System.err, sb)
        }

        process.waitFor()

        return sb.toString()
    }

    static void connectStream(InputStream forkedProcessOutput, PrintStream currentProcessOutput, StringBuffer sb) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(forkedProcessOutput))
        String line
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n")
            currentProcessOutput.println(line)
        }
    }
}
