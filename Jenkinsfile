pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean install -Pjenkins -Dmaven.test.skip=true'
      }
    }
    stage('Test') {
      steps {
        sh 'mvn verify jacoco:report-aggregate -Dmaven.test.skip=false'
      }
    }
    stage('Report') {
      parallel {
        stage('Report') {
          steps {
            junit(testResults: '**/target/*-reports/*.xml', healthScaleFactor: 50)
          }
        }
        stage('deployment on maven.inria.fr') {
          steps {
            sh 'mvn deploy -Pmaven-inria-fr-release'
          }
        }
      }
    }
  }
}