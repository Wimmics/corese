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
      steps {
        junit(testResults: '**/target/surefire-reports/*.xml', healthScaleFactor: 50)
      }
    }
  }
}