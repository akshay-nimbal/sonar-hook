# Pre-commit static code analysis using SonarQube.

Static code analysis is an essential part of every software project and no surprise why SonarQube is part of every CI-CD plans nowadays. SonarQube helps to detect tricky bugs against a set of active rules categorized into Code smells, Bugs, Vulnerabilities etc in the code. It supports more than 25+ programming languages. Without going much deeper into how SonarQube works, lets discuss one of the use case of it. Follow SonarQube for more information. Every code versioning client for ex: GIT, SVN etc allows you to configure a pre-commit hook of your choice. Pre-commit is one of the many life cycles. of the commit-push actions. We will discuss on how to set up a pre-commit hook of sonarqube for SVN.For this use case I've implemented a Java application which will

1. Read your modified files from your directory to be scanned.
2. Use Sonar Runner to push them to sonarqube server that is running.
3. Get the results once the scan is completed.
4. Based on the status of the Quality Gate, the commit is either completed or failed.
## Configuration and Usage

Step 1: Download the latest [Sonarqube server](https://www.sonarqube.org/downloads/). 
Download the [sonar-runner-2.4](http://repo1.maven.org/maven2/org/codehaus/sonar/runner/sonar-runner-dist/2.4/sonar-runner-dist-2.4.jar).

Step 2: Step 2: Download the hook script and sonar-hook.jar from my github repository 
[akstay/sonar-hook](https://github.com/akstay/sonar-hook/tree/master/SonarSVNHook/). 
Place the sonarqube-svn-hook directory separately. This has hook script and jar required. You can find the logs the same directory.

Step 3: Configure the following environment variables:

Variable name: SONAR_HOOK_HOME Value: D:\Sonar-SVN-Hook\SonarSVNHook\sonarqube-svn-hook i.e where your downloaded jar and hook file from github are placed.

Variable name: SONAR_RUNNER_HOME Value: D:\softwares\sonar-runner-2.4 i.e where you downloaded sonar-runner jar is present.
Add to Path as %SONAR_RUNNER_HOME%\bin.

Goto cmd, check if your sonar-runner is installed globally as below.
```bash
> sonar-runner
SonarQube Runner 2.4
Java 1.8.0_121 Oracle Corporation (64-bit)
Windows 10 10.0 amd64
INFO: Runner configuration file: D:\softwares\sonar-runner-2.4\conf\sonar-runner.properties
INFO: Project configuration file: NONE
INFO: Default locale: "en_US", source code encoding: "windows-1252" (analysis is platform dependent)
INFO: Work directory: C:\Users\akshay.n\.\.sonar
INFO: SonarQube Server 7.7.0.23042
```
Step 4: If you want the sonarqube server to run locally. Goto sonarqube server that you downloaded in Step 1. i.e. sonarqube-7.7\bin\windows-x86-64 run StartSonar.bat.
```bash
.
.
.
.

jvm 1    | 2019.09.28 01:49:23 INFO  app[][o.s.a.SchedulerImpl] Process[ce] is up
jvm 1    | 2019.09.28 01:49:23 INFO  app[][o.s.a.SchedulerImpl] SonarQube is up
```

Step 5: Configuring the SVN pre-commit hook. Goto Tortise SVN settings, configure hook script as below where working copy path will be your project directory to be scanned before commit.
![alt text](https://github.com/akstay/sonar-hook/blob/master/SonarSVNHook/test/precomit.PNG)

Goto sonar-project.properties that is present in the download github repository. Change sonar.host.url if needed.
```bash
#//sonar.host.url is mandatory property. Change this with your sonarqube server
sonar.host.url = http://127.0.0.1:9000

# // Optional fields when you want to scan your custom project directory. If not enabled the directory mentioned in the hook settings will be considered.
#sonar.scan.directory = D:\\UI\\main\\src\\app\\services

# // optional field when you want custom project key. If not enabled your system username will be taken as projectkey.

#sonar.projectKey = myProject
```
Step 6: Last step in the configuration is to check if SVN command line tools is enabled.

If not enabled, install SVN again with command line tools enabled while installation.
![alt text](https://github.com/akstay/sonar-hook/blob/master/SonarSVNHook/test/svn-cmd.PNG)

Step 7: We are ready to test the setup. Goto your project directory which is configured in hook settings. As soon as you try to commit, the hook script triggers the Java application which runs sonar-runner and publishes the report to SonarQube server. Meanwhile you can check the execution logs present in logs folder. It waits till scan gets completed and if the Quality gate of the current scan fails, the hook script stops executing as below
![alt text](https://github.com/akstay/sonar-hook/blob/master/SonarSVNHook/test/hookeror.PNG)

Step 8: Goto 127.0.0.1:9000 where your Sonar server is running. Find your project against your system username.
![alt text](https://github.com/akstay/sonar-hook/blob/master/SonarSVNHook/test/sonar3.PNG)

That's it. We are done.

## Upcoming features
1) Support for GIT.
2) Support for sonarsource.


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
