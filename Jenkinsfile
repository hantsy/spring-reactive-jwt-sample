pipeline {
    agent {
        docker {
            image 'maven:3.8.4-openjdk-17'
            args '-v /root/.m2:/root/.m2'
        }
    }
    options {
        skipStagesAfterUnstable()
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Test') {
            steps {
			    sh 'docker-compose up -d mongodb'
				sh 'sleep 10'
				sh 'docker ps -aq'
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        #stage('Deliver') {
        #    steps {
        #        sh './jenkins/scripts/deliver.sh'
        #    }
        #}
    }
}