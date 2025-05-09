pipeline{
    agent {label 'Jenkins-Agent'}
    tools {
        jdk 'JDK17'
        maven 'MAVEN3'
    }
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
    }
    stages{
        stage ('clean Workspace'){
            steps{
                cleanWs()
            }
        }
        stage('Get Previous Successfully Build Number') {
          steps {
            script {

               def previousBuildNumber = currentBuild.previousSuccessfulBuild?.number
               if (previousBuildNumber == null) {
                   env.IMAGE_TAG_VERSION = 'latest'
               } else {
                   def imageTag = previousBuildNumber ?: 'latest'
                   env.IMAGE_TAG_VERSION = imageTag
                   echo "Previous build TAG set as env variable: ${env.IMAGE_TAG_VERSION}"
                   sh "sudo docker rmi devsahamerlin/tasksmanager:${env.IMAGE_TAG_VERSION} -f"
               }
            }
          }
        }

        stage('Clean existing containers') {
            steps {
               script {
                  sh '''
                  if [ ! "$(docker ps -a -q -f name=tasksmanager)" ]; then
                      echo "Found running container ID"
                      if [ "$(docker ps -aq -f name=tasksmanager)" ]; then
                          echo "Found running container"
                          docker rm -f $(sudo docker ps -aq)
                      else
                        echo "No matching container found."
                      fi
                  fi'''
               }
            }
        }
        stage ('checkout scm') {
            steps {
                script {
                    git branch: 'main',
                    credentialsId: 'merlin-github-user-credentials',
                    url: 'https://github.com/devsahamerlin/acn-devsecops-upskills.git'
                }
            }
        }
//         stage ('maven compile') {
//             steps {
//                 sh 'mvn clean compile'
//             }
//         }
//         stage ('maven Test') {
//             steps {
//                 sh 'mvn test'
//             }
//         }
//
//         stage ('maven clean verify') {
//             steps {
//                 sh 'mvn clean verify'
//             }
//         }
//
//
//         stage("Sonarqube Analysis "){
//             steps{
//                 withSonarQubeEnv('merlin-sonar-server') {
//                     sh ''' mvn sonar:sonar \
//                     -Dsonar.projectName=merlin-acn-upskills \
//                     -Dsonar.java.binaries=. \
//                     -Dsonar.projectKey=merlin-acn-upskills-key '''
//                 }
//             }
//         }
//
//         stage("quality gate"){
//             steps {
//                 script {
//                   waitForQualityGate abortPipeline: false, credentialsId: 'merlin-sonar-token'
//                 }
//            }
//         }
//         stage ('Build Jar file'){
//             steps{
//                 sh 'mvn clean install'
//                 sh 'mkdir -p src/main/resources/static/jacoco'
//                 sh 'cp -r target/site/jacoco/* src/main/resources/static/jacoco/'
//             }
//         }

//         stage('TRIVY FS SCAN') {
//            steps {
//                sh '''
//                     trivy fs --format table .
//                     trivy fs --format table --exit-code 1 --severity CRITICAL .
//                '''
//            }
//         }
//
//
//         stage("OWASP Dependency Check"){
//             steps{
//                 withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
//                     dependencyCheck additionalArguments: "--scan ./ --format XML --nvdApiKey ${NVD_API_KEY}", odcInstallation: 'DPD-Check'
//                     dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
//                 }
//             }
//         }

        stage('Build and Push Docker Image') {
           environment {
             DOCKER_IMAGE = "devsahamerlin/tasksmanager:${BUILD_NUMBER}"
             REGISTRY_CREDENTIALS = credentials('merlin-docker')
           }
           steps {
             script {
                sh "tree"
                "sudo docker images | grep devsahamerlin/tasksmanager*"
                sh "sudo docker build -t ${DOCKER_IMAGE} ."
                def dockerImage = docker.image("${DOCKER_IMAGE}")
                 docker.withRegistry('https://index.docker.io/v1/', "merlin-docker") {
                     dockerImage.push()
                 }
             }
           }
        }
//         stage("TRIVY DOCKER IMAGE SCAN"){
//             steps{
//                 sh "trivy image devsahamerlin/tasksmanager:${BUILD_NUMBER} --format table"
//                 //sh "trivy image devsahamerlin/tasksmanager:${BUILD_NUMBER} --format table --exit-code 1 --severity CRITICAL"
//             }
//         }

        stage ('Deploy to container'){
            steps{
                sh """
                    sudo docker ps -a --filter name=merlin-tasksmanager -q | xargs -r sudo docker stop
                    sudo docker ps -a --filter name=merlin-tasksmanager -q | xargs -r sudo docker rm -f
                    sudo docker images devsahamerlin/tasksmanager -q | xargs -r sudo docker rmi -f
                    sudo docker run -d --name merlin-asksmanager -p 8083:8082 devsahamerlin/tasksmanager:${BUILD_NUMBER}
                """
            }
        }

        stage('Run Selenium Tests') {
            steps {
                sh 'sleep 20'
                sh 'mvn -Dtest=TaskManagerSelenium test'
            }
        }

        stage('Update Deployment File') {
                environment {
                    GIT_REPO_NAME = "acn-devsecops-upskills"
                    GIT_USER_NAME = "devsahamerlin"
                }
                steps {
                    withCredentials([string(credentialsId: 'merlin-github-user-credentials', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            git config user.email "devsahamerlin@gmail.com"
                            git config user.name "Saha Merlin Jenkins"
                            BUILD_NUMBER=${BUILD_NUMBER}
                            sed -i "s/${IMAGE_TAG_VERSION}/${BUILD_NUMBER}/g" k8s/manifests/deployment.yml
                            git add k8s/manifests/deployment.yml
                            git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                            git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                        '''
                    }
                }
        }
    }

//     post {
//         always {
//             //archiveArtifacts artifacts: 'trivyfs.txt', fingerprint: true
//         }
//
//         failure {
//             //echo 'Build failed due to HIGH or CRITICAL vulnerabilities found by Trivy.'
//         }
//     }
}