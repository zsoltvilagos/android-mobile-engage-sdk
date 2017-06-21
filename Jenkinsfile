node {
    stage('init'){
        deleteDir()
        def lines = sh(script: "${env.ANDROID_HOME}platform-tools/adb devices", returnStdout: true)
        echo "$lines"
        git url: 'git@github.com:emartech/android-mobile-engage-sdk.git', branch: 'master'
    }
    
    stage('build'){
        sh './gradlew clean build -x lint -x test'
        archiveArtifacts '**/*.aar'
    }
    
    stage('lint'){
        sh './gradlew lint'
        archiveArtifacts '**/lint-results*.*'
    }
    
    stage('test'){
        parallel unit: {
            try{
                sh './gradlew test'
            } catch(e){
                currentBuild.result = 'FAILURE'
                throw e
            } finally {
                junit '**/test-results/**/*.xml'
                try{
                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/test-results/**/*.xml'
                }catch(e){}
            }
        }, instrumentation: {
            try {
                lock(env.ANDROID_DEVICE_FARM_LOCK){
                    retry(2){
                        sh './gradlew cAT'
                    }
                }
            } catch (e){
                currentBuild.result = 'FAILURE'
                throw e
            } finally {
                junit '**/outputs/androidTest-results/connected/*.xml'
                archiveArtifacts '**/outputs/androidTest-results/connected/*.xml'    
            }
        },
        failFast: false
    }
    
    stage('local-maven-deploy'){
        sh './gradlew install'
    }
    
    def version = sh (script: 'git describe', returnStdout: true).trim()
    if(version ==~ /\d\.\d\.\d/){
        stage('release-bintray'){
    	    build job: 'utils/slack-message', parameters: [string(name: 'slackText', value: "Releasing MobileEngage SDK $version.")]
    	    sh './gradlew bintrayUpload'
    	    build job: 'utils/slack-message', parameters: [string(name: 'slackText', value: "MobileEngage SDK $version released to Bintray.")]
    	}
    }
}