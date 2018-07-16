pipeline {
  agent any
  stages {
    stage('Build') {
      parallel {
        stage('Build') {
          steps {
            sh 'mvn clean install -Pjenkins -Dmaven.test.skip=true'
          }
        }
        stage('display value') {
          steps {
            sh "echo \"env.DEPLOY_TO_CENTRAL = ${env.DEPLOY_TO_CENTRAL}\""
          }
        }
      }
    }
    stage('Test') {
      steps {
        sh 'mvn verify jacoco:report-aggregate -Dmaven.test.skip=false'
      }
    }
    stage('deployment on maven.inria.fr') {
      steps {
        sh 'mvn deploy -Pmaven-inria-fr-release -Dmaven.test.skip=true'
      }
    }
    stage('test on artefacts at maven.inria.fr') {
      steps {
        sh '''rm -fr ${HOME}/.m2/repository/fr/inria/corese
mvn -U test verify -Pmaven-inria-fr-release'''
      }
    }
    stage('Report') {
      parallel {
        stage('Report') {
          steps {
            junit(testResults: '**/target/*-reports/*.xml', healthScaleFactor: 50)
          }
        }
        stage('error') {
          steps {
            jacoco(classPattern: '**/classes', execPattern: '**/**.exec', inclusionPattern: '**/*.class', sourcePattern: '**/src/main/java')
          }
        }
      }
    }
    stage('Deploy on maven ossrh (maven central)') {
      steps {
        script {
          if (env.DEPLOY_TO_CENTRAL == 'true') {
            echo 'deploying'
            sh 'mvn deploy -Pmaven-central-release -Dmaven.test.skip=true'
          } else {
            echo 'not deploying since property deployOnMavenCentral is not set in the root pom.xml'
          }
        }

      }
    }
  }
  environment {
    DEPLOY_TO_CENTRAL = readMavenPom().getProperties().getProperty("deployOnMavenCentral")
  }
}
