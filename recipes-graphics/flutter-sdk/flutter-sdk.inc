SUMMARY = "Flutter - Dart based UI framework SDK"
WEBSITE = "https://flutter.dev/"

S = "${WORKDIR}/flutter"

do_install() {

    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}

FILES_${PN}-dev = "${datadir}/flutter/sdk/* ${datadir}/flutter/sdk/.*"

INSANE_SKIP_${PN} = "already-stripped"

BBCLASSEXTEND =+ " native nativesdk"
