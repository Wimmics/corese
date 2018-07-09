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
			stage('deployment on maven.inria.fr') {
				steps {
					sh 'mvn deploy -Pmaven-inria-fr-release'
				}
			} 
			stage('test on artefacts at maven.inria.fr') {
				steps {
					sh 'mvn test verify -Pmaven-inria-fr-release'
				}
			}
			stage('Report') {
				steps {
					junit(testResults: '**/target/*-reports/*.xml', healthScaleFactor: 50)
								       }
								       }
			stage('Deploy on maven ossrh (maven central)') {
				steps {
					sh 'mvn deploy -Pmaven-central-release'
				}
			}
								       }
								       }
