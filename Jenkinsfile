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

stages {
    stage('Checkout') {
        steps {
            echo 'Checking out ePlatform-App source code'
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

    stage('Clean') {
        steps {
            bat 'mvn clean'
        }
    }

    stage('Compile') {
        steps {
            bat 'mvn compile'
        }
    }

    stage('Unit Tests') {
        steps {
            bat 'mvn test'
        }
    }

    stage('Package') {
        steps {
            bat 'mvn package'
        }
    }
}

post {
    always {
        junit testResults: '**/target/surefire-reports/TEST-*.xml',
              allowEmptyResults: true
    }

    success {
        echo 'SUCCESS: ePlatform-App CI pipeline completed!'
    }

    failure {
        echo 'FAILURE: Check the Jenkins console output.'
    }
}

}
