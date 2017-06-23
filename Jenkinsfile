@Library(['android-pipeline', 'general-pipeline']) _

node {
    withSlack channel: 'jenkins', {
        stage("init") {
            deleteDir()
            deviceCount shouldBe: env.ANDROID_DEVICE_COUNT,
                    action: { devices, message ->
                        slackMessage channel: 'jenkins', text: message
                    }
            git url: 'git@github.com:emartech/android-mobile-engage-sdk.git', branch: 'master'
        }

        stage("build") {
            build andArchive: '**/*.aar'
        }

        stage('lint') {
            lint andArchive: '**/lint-results*.*'
        }

        stage("unit-test") {
            test andArchive: '**/test-results/**/*.xml'
        }

        stage("instrumentation-test") {
            instrumentationTest withScreenOn: false, withLock: env.ANDROID_DEVICE_FARM_LOCK, andArchive: '**/outputs/androidTest-results/connected/*.xml'
        }

        stage('local-maven-deploy') {
            sh './gradlew install'
        }

        def version = sh(script: 'git describe', returnStdout: true).trim()
        def statusCode = sh returnStdout: true, script: "curl -I https://jcenter.bintray.com/com/emarsys/mobile-engage-sdk/$version/ | head -n 1 | cut -d\$' ' -f2".trim()
        def releaseExists = "200" == statusCode
        if (version ==~ /\d\.\d\.\d/ && !releaseExists) {
            stage('release-bintray') {
                slackMessage channel: 'jenkins', text: "Releasing Mobile Engage SDK $version."
                sh './gradlew bintrayUpload'
                slackMessage channel: 'jenkins', text: "Mobile Engage SDK $version released to Bintray."
            }
        }
    }
}