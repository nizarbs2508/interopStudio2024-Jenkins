pipeline {
    agent any
     tools {
        maven 'Maven 3.9.9'  // Nom de l'installation que vous avez configurée
    }
    stages {
        stage('Cloner le code') {
            steps {
                // Spécifier la branche 'main' explicitement pour le checkout
                git branch: 'main', url: 'https://github.com/nizarbs2508/Interop2024.git'
            }
        }
        stage('Construire') {
            steps {
                sh 'mvn clean install assembly:single -Dtest.skip=true'
            }
        }
        stage('Créer l’image Docker') {
            steps {
                sh 'docker build -t nizarbsalem/Interop2024:latest .'
            }
        }
        stage('Pousser sur Docker Hub') {
            steps {
                withDockerRegistry([ credentialsId: 'docker-hub-credentials', url: '' ]) {
                    sh 'docker push nizarbsalem/Interop2024:latest'
                }
            }
        }
        stage('Déployer') {
            steps {
                sh 'docker run -d -p 8080:8080 nizarbsalem/Interop2024:latest'
            }
        }
    }
}
