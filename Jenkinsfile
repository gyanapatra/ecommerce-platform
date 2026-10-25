pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }
    
    triggers {
    	pollSCM('H/2 * * * *')
	}

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out ePlatform source code'
                checkout scm
            }
        }

        stage('Environment Validation') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'git --version'
            }
        }

        stage('Clean and Verify') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('Validate JaCoCo Reports') {
            steps {
                script {
                    def htmlReport = 'auth-service/target/site/jacoco/index.html'
                    def xmlReport = 'auth-service/target/site/jacoco/jacoco.xml'

                    if (!fileExists(htmlReport)) {
                        error "JaCoCo HTML report not found: ${htmlReport}"
                    }

                    if (!fileExists(xmlReport)) {
                        error "JaCoCo XML report not found: ${xmlReport}"
                    }

                    echo 'JaCoCo HTML and XML reports generated successfully.'
                }
            }
        }

        stage('Validate Executable JAR') {
            steps {
                script {
                    def jarPath = 'auth-service/target/auth-service-1.0.0-SNAPSHOT.jar'

                    if (!fileExists(jarPath)) {
                        error "Executable Auth Service JAR not found: ${jarPath}"
                    }

                    echo "Validated executable JAR: ${jarPath}"
                }
            }
        }

        stage('Generate SHA-256 Checksum') {
            steps {
                bat '''
                    certutil -hashfile "auth-service\\target\\auth-service-1.0.0-SNAPSHOT.jar" SHA256 > "auth-service\\target\\auth-service-1.0.0-SNAPSHOT.jar.sha256.txt"
                    if errorlevel 1 exit /b 1
                '''
            }
        }
    }

    post {
        always {
            junit(
                testResults: '**/target/surefire-reports/TEST-*.xml',
                allowEmptyResults: true
            )
            
            recordCoverage(
	            tools: [[
	                parser: 'JACOCO',
	                pattern: 'auth-service/target/site/jacoco/jacoco.xml'
	            ]]
        	)

            archiveArtifacts(
                artifacts: 'auth-service/target/auth-service-1.0.0-SNAPSHOT.jar,auth-service/target/auth-service-1.0.0-SNAPSHOT.jar.sha256.txt,auth-service/target/site/jacoco/**',
                allowEmptyArchive: true,
                fingerprint: true
            )
        }

        success {
            echo 'SUCCESS: ePlatform CI pipeline completed!'
        }

        failure {
            echo 'FAILURE: Check the Jenkins console output.'
        }
    }
}
