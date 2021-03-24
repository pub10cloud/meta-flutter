DESCRIPTION = "Flutter Engine"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "9e5072f0ce81206b99db3598da687a19ce57a863"

ENGINE_URI ?= "git@github.com:flutter/engine.git"

S = "${WORKDIR}/git/src"

inherit python3native native

require gn-utils.inc
require flutter-engine.inc

DEPENDS =+ " flutter-engine"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

GN_ARGS = ""

do_configure() {

    ./flutter/tools/gn --unoptimized
}

do_compile() {

	ninja -C out/host_debug_unopt -v
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    cd ${S}/out/host_debug_unopt

    install -d ${D}${bindir}
    install -m 755 dart ${D}${bindir}

    install -d ${D}${datadir}/flutter/engine
    install -m 644 frontend_server.dart.snapshot ${D}${datadir}/flutter/engine
}

FILES_${PN} = " \
    ${bindir}/dart \
    ${datadir}/flutter/engine/frontend_server.dart.snapshot \
    "

SYSROOT_DIRS_NATIVE =+ " \
    ${datadir}/flutter/engine \
    "

INSANE_SKIP_${PN} += "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
