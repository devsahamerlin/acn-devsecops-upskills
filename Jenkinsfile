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
        stage ('🧹 Clean Workspace'){
            steps{
                cleanWs()
            }
        }
        stage('♻️ Restore Previous Artifacts') {
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

        stage('🧼 Clean Containers') {
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
        stage ('📥 Checkout Source Code') {
            steps {
                script {
                    git branch: 'main',
                    credentialsId: 'merlin-github-user-credentials',
                    url: 'https://github.com/devsahamerlin/acn-devsecops-upskills.git'
                }
            }
        }
        stage ('🛠️ Compile with Maven') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage ('✅ Run Unit Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage ('🔁 Verify Build') {
            steps {
                sh 'mvn clean verify'
            }
        }

//
//         stage("📊 (SAST) SonarQube Analysis"){
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
//         stage("🚦 (SAST) SonarQube Quality Gate"){
//             steps {
//                 script {
//                   waitForQualityGate abortPipeline: false, credentialsId: 'merlin-sonar-token'
//                 }
//            }
//         }
        stage ('📦 Package JAR'){
            steps{
                sh 'mvn clean install'
                sh 'mkdir -p src/main/resources/static/jacoco'
                sh 'cp -r target/site/jacoco/* src/main/resources/static/jacoco/'
            }
        }

//         stage('🔍 Trivy Filesystem Scan') {
//            steps {
//                sh '''
//                     trivy fs --format table .
//                     trivy fs --format table --exit-code 1 --severity CRITICAL .
//                '''
//            }
//         }
//
//
//         stage("🧪 (SCA) OWASP Dependency Check"){
//             steps{
//                 withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
//                     dependencyCheck additionalArguments: "--scan ./ --format XML --nvdApiKey ${NVD_API_KEY}", odcInstallation: 'DPD-Check'
//                     dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
//                 }
//             }
//         }

        stage('🚀 Build & Push Docker Image') {
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
//         stage("🐳 Trivy Docker Image Scan"){
//             steps{
//                 sh "trivy image devsahamerlin/tasksmanager:${BUILD_NUMBER} --format table"
//                 //sh "trivy image devsahamerlin/tasksmanager:${BUILD_NUMBER} --format table --exit-code 1 --severity CRITICAL"
//             }
//         }

//         stage ('📡 Deploy Container'){
//             steps{
//                 sh """
//                     sudo docker ps -a --filter name=merlin-tasksmanager -q | xargs -r sudo docker stop
//                     sudo docker ps -a --filter name=merlin-tasksmanager -q | xargs -r sudo docker rm -f
//                     sudo docker images devsahamerlin/tasksmanager -q | xargs -r sudo docker rmi -f
//                     sudo docker run -d --name merlin-tasksmanager -p 8083:8082 devsahamerlin/tasksmanager:${BUILD_NUMBER}
//                 """
//             }
//         }


        stage('Push to Git (for ArgoCD GitOps)') {
                environment {
                    GIT_REPO_NAME = "acn-devsecops-upskills"
                    GIT_USER_NAME = "devsahamerlin"
                }
                steps {
                    withCredentials([string(credentialsId: 'merlin-gitops-user-secret-text', variable: 'GITHUB_TOKEN')]) {
                        sh '''
                            git config user.email "devsahamerlin@gmail.com"
                            git config user.name "DevOps Team Jenkins"
                            BUILD_NUMBER=${BUILD_NUMBER}
                            sed -i "s/${IMAGE_TAG_VERSION}/${BUILD_NUMBER}/g" k8s/manifests/deployment.yml
                            git add k8s/manifests/deployment.yml
                            git commit -m "Update deployment image to version ${IMAGE_TAG_VERSION} - ${BUILD_NUMBER}"
                            git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:devops
                        '''
                    }
                }
        }

//         stage('🤖 Run Selenium UI Tests') {
//             steps {
//                 sh 'sleep 30'
//                 sh 'mvn -Dtest=TaskManagerSelenium test'
//             }
//         }
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