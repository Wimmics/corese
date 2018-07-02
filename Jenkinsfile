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
        sh 'mvn test jacoco:report-aggregate -Dmaven.test.skip=false'
        junit(testResults: 'build/reports/**/*.xml', allowEmptyResults: true, healthScaleFactor: 50)
      }
    }
  }
}