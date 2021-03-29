GCLIENT_SYNC_OPT = "--force --reset -D --no-history "
GCLIENT_RUNHOOKS_OPT = ""

DEPENDS =+ " ninja-native depot-tools-native flutter-sdk-native tar-native xz-native"

addtask gclient_fetch    before do_patch after do_unpack
addtask gclient_runhooks before do_configure after do_patch

do_gclient_fetch() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export CURL_CA_BUNDLE=${STAGING_BINDIR_NATIVE}/depot_tools/ca-certificates.crt
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}
    export SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
    export SSH_AGENT_PID=${SSH_AGENT_PID}

    ENGINE_REV=$(cat ${STAGING_DATADIR_NATIVE}/flutter/sdk/bin/internal/engine.version)

    cd ${S}/..

    gclient.py config --spec 'solutions = [
        {
            "managed" : False,
            "name" : "src/flutter",
            "url" : "'${ENGINE_URI}'",
            "custom_vars" : {
                "download_android_deps" : False,
                "download_windows_deps" : False,
            }
        }
    ]'

    rm -rf ${S}/*

    gclient.py sync --nohooks --noprehooks ${GCLIENT_SYNC_OPT} --revision ${ENGINE_REV} ${PARALLEL_MAKE} -v
}

do_gclient_fetch[depends] =+ " \
    depot-tools-native:do_populate_sysroot \
    flutter-sdk-native:do_populate_sysroot \
    tar-native:do_populate_sysroot \
    xz-native:do_populate_sysroot \
    "

do_gclient_runhooks() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}

    cd ${S}
    gclient.py runhooks ${GCLIENT_RUNHOOKS_OPT} ${PARALLEL_MAKE}  -v
}

# vim:set ts=4 sw=4 sts=4 expandtab:
