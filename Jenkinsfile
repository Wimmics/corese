pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean install -Pjenkins'
      }
    }
    stage('Test') {
      steps {
        sh './gradlew check'
        junit(testResults: 'build/reports/**/*.xml', allowEmptyResults: true, healthScaleFactor: 50)
      }
    }
  }
}