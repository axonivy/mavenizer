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
		  deployP2Repository('features/mavenizer/nightly')
		  
        }
	    archiveArtifacts 'designer.project.maven.p2/target/*.zip'
      }
    }
  }
}

def deployP2Repository(def folderName) {
  sshagent(['zugprojenkins-ssh']) {
    def host = 'axonivy1@217.26.54.241'
    def destFolder = "/home/axonivy1/data/p2/$folderName"

    echo "Upload p2 repository to $host:$destFolder"
    sh "ssh $host mkdir -p $destFolder"
    sh "rsync -r designer.project.maven.p2/target/repository/ $host:$destFolder"
    sh "ssh $host touch $destFolder/p2.ready"
  }
}
