def STATUS = ['SUCCESS': 'good', 'FAILURE': 'danger', 'UNSTABLE': 'danger', 'ABORTED': 'danger']

pipeline {
  agent any
  environment {
    REPOSITORY_NAME = "ctdatabridge"
    CODE_BASE_DIR = "${WORKSPACE}/CTDataBridge"
    ARTIFACTORY_URL = "artifactory.build.ingka.ikea.com"
    ARTIFACT_NAME = "${REPOSITORY_NAME}_${BRANCH_NAME}_${BUILD_ID}"
    DOCKER_ARTIFACT_NAME = "${REPOSITORY_NAME}/${BRANCH_NAME}/${REPOSITORY_NAME}:${BUILD_ID}"
    scannerHome = tool 'SonarQubeScanner v4.2.0.1873'
  }
  options {
    skipDefaultCheckout true
  }
  tools {
    maven 'Maven 3.6.2' 
    jdk 'jdk8'     
  }
  stages {
    stage('Prepare Env') {
      steps {
        script {
          if(env.BRANCH_NAME != "master" && env.BRANCH_NAME != "release-to-test" ){
            env.ARTIFACTORY_GENERIC_REPOSITORY = "cwis-generic-dev-local"
            env.ARTIFACTORY_DOCKER_REPOSITORY = "cwis-docker-dev-local"
            if(env.BRANCH_NAME == "release-to-dev")
              env.SONAR_QUBE_TARGET_BRANCH = "master"
            else
              env.SONAR_QUBE_TARGET_BRANCH = "release-to-dev"
          }
          else if(env.BRANCH_NAME == "release-to-test"){
            env.ARTIFACTORY_GENERIC_REPOSITORY = "cwis-generic-dev-local"
            env.ARTIFACTORY_DOCKER_REPOSITORY = "cwis-docker-dev-local"
            env.SONAR_QUBE_TARGET_BRANCH = "master"
          }
          else if(env.BRANCH_NAME == "master"){
            env.ARTIFACTORY_GENERIC_REPOSITORY = "cwis-generic-release-local"
            env.ARTIFACTORY_DOCKER_REPOSITORY_DEV = "cwis-docker-release-local"
            env.SONAR_QUBE_TARGET_BRANCH = "master"
          }
          env.DOCKER_IMAGE_NAME = "${ARTIFACTORY_URL}/${ARTIFACTORY_DOCKER_REPOSITORY}/${DOCKER_ARTIFACT_NAME}"
        }
      }
    }
    stage('Checkout SCM') {
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
          checkout(scm: scm, poll: true)
        }
      }
    }
    stage('Compile') {
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
         	configFileProvider([configFile(fileId: '7b1aaa61-9635-4204-a1e0-b324a8891e0b', variable: 'FEEDER_SETTINGS_XML')]) {
        		sh 'mvn -s $FEEDER_SETTINGS_XML compile -DskipTests'
    		  }
        }
      }
    }
    stage('Test') {
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
          configFileProvider([configFile(fileId: '7b1aaa61-9635-4204-a1e0-b324a8891e0b', variable: 'FEEDER_SETTINGS_XML')]) {
        		sh 'mvn -s $FEEDER_SETTINGS_XML test'
    		  }
        }
      }
    }
    stage('Static Code Analysis') {
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
          withSonarQubeEnv('SonarQube Prod') {
           sh '${scannerHome}/bin/sonar-scanner -Dsonar.branch.name=$BRANCH_NAME -Dsonar.branch.target=${SONAR_QUBE_TARGET_BRANCH}'
          }
        }
      }
    }
    stage('Package') {
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
          configFileProvider([configFile(fileId: '7b1aaa61-9635-4204-a1e0-b324a8891e0b', variable: 'FEEDER_SETTINGS_XML')]) {
        		sh 'mvn -s $FEEDER_SETTINGS_XML package -Dmaven.test.skip=true'
    		  }
        }
      }
    }
    // stage('Zip Artifacts') {
    //   when {
    //     branch 'release-to-dev'
    //   }
    //   steps {
    //     dir(path: "${env.CODE_BASE_DIR}") {
    //       sh '''
    //         mkdir ${REPOSITORY_NAME}
    //         cp target/*.jar ${REPOSITORY_NAME}
    //         cp Dockerfile ${REPOSITORY_NAME}
    //       '''       
    //       zip zipFile: "${env.ARTIFACT_NAME}.zip", archive: false, dir: "${env.REPOSITORY_NAME}"
    //     }
    //   }
    // }
    stage('Build Docker Image') {
     when {
        branch 'release-to-dev'
      }
      steps {
        dir(path: "${env.CODE_BASE_DIR}") {
          container('docker') {
            script {

              docker.withRegistry("https://${env.ARTIFACTORY_URL}", 'CWISArtifactoryUser') {
                customImage = docker.build("${env.DOCKER_IMAGE_NAME}")
                /* Push the container to the custom Registry */
                customImage.push('latest')
              }
             }
           }
        }
      }
    }
  }
  post {
    always {
      echo "*${currentBuild.currentResult}:* build #${env.BUILD_ID} for ${env.JOB_NAME} on ${env.JENKINS_URL}"
      slackSend(color: STATUS[currentBuild.currentResult], message: "*${currentBuild.currentResult}:* build #${env.BUILD_ID} for ${env.JOB_NAME} on ${env.JENKINS_URL}")
    }
  }
}