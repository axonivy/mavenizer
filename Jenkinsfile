pipeline {
  agent {
    dockerfile true
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '2'))
  }

  triggers {
    pollSCM '@hourly'
    cron '@midnight'
  }

  stages {
    stage('build') {
      steps {
        script {
          maven cmd: 'clean verify'
          deployP2Repo srcDir: 'designer.project.maven.p2/target/repository/',
                                 destDir: 'features/mavenizer/7.3'
        }
        archiveArtifacts 'designer.project.maven.p2/target/*.zip'
      }
    }
  }
}
