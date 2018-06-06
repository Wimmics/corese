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
        sh '''export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64/
./gradlew check'''
        junit(testResults: 'build/reports/**/*.xml', allowEmptyResults: true, healthScaleFactor: 50)
      }
    }
  }
}