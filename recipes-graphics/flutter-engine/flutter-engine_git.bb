DESCRIPTION = "Flutter Engine"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"

FILESEXTRAPATHS_prepend_poky := "${THISDIR}/files:"
ENGINE_URI ?= "git@github.com:flutter/engine.git"
SRC_URI = "file://sysroot_gni.patch \
           file://custom_BUILD_gn.patch \
           "

S = "${WORKDIR}/git/src"

inherit python3native

require gn-utils.inc
require flutter-engine.inc

DEPENDS =+ " freetype"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

PACKAGECONFIG ??= "embedder-for-target full-dart-sdk fontconfig skshaper stripped lto no-goma"

PACKAGECONFIG[clang] = "--clang"
PACKAGECONFIG[no-clang] = "--no-clang"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[no-static-analyzer] = "--no-clang-static-analyzer"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[no-full-dart-sdk] = "--no-full-dart-sdk"
PACKAGECONFIG[build-glfw-shell] = "--build-glfw-shell"
PACKAGECONFIG[no-build-glfw-shell] = "--no-build-glfw-shell"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[no-skshaper] = "--no-enable-skshaper"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[lto] = "--lto"
PACKAGECONFIG[no-lto] = "--no-lto"
PACKAGECONFIG[stripped] = "--stripped"
PACKAGECONFIG[no-stripped] = "--no-stripped"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[goma] = "--goma"
PACKAGECONFIG[no-goma] = "--no-goma"
PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[mode-debug] = "--runtime-mode debug"
PACKAGECONFIG[mode-profile] = "--runtime-mode profile"
PACKAGECONFIG[mode-release] = "--runtime-mode release"
PACKAGECONFIG[mode-jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[no-dart-version-git-info] = "--no-dart-version-git-info"

GN_ARGS = " \
  ${PACKAGECONFIG_CONFARGS} \
  --target-os linux \
  --linux-cpu ${@gn_target_arch_name(d)} \
  --target-sysroot ${STAGING_DIR_TARGET} \
  --target-triple ${@gn_clang_triple_prefix(d)} \
  --target-toolchain ${S}/buildtools/linux-x64/clang \
  "

do_patch() {
    cd ${S}
    git apply ../../sysroot_gni.patch
    git apply ../../custom_BUILD_gn.patch
}

ARGS_GN_FILE = "${S}/${@get_out_dir(d)}/args.gn"

ARGS_GN_APPEND = " \
    arm_tune = \"${TUNEABI}\" \
    arm_float_abi = \"${TARGET_FPU}\" \
    "

FLUTTER_TRIPLE = "${@gn_clang_triple_prefix(d)}"

TARGET_GCC_VERSION ?= "9.3.0"
TARGET_CLANG_VERSION ?= "12.0.0"
do_configure() {

    bbnote "./flutter/tools/gn ${GN_ARGS}"
    bbnote "echo ${ARGS_GN_APPEND} >> ${ARGS_GN_FILE}"

    cd ${S}

    ./flutter/tools/gn ${GN_ARGS} --disable-desktop-embeddings

    echo ${ARGS_GN_APPEND} >> ${ARGS_GN_FILE}

    # libraries required for linking so
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/${TARGET_GCC_VERSION}/crtbeginS.o ${S}/buildtools/linux-x64/clang/lib/clang/${TARGET_CLANG_VERSION}/lib/${FLUTTER_TRIPLE}/
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/${TARGET_GCC_VERSION}/crtendS.o ${S}/buildtools/linux-x64/clang/lib/clang/${TARGET_CLANG_VERSION}/lib/${FLUTTER_TRIPLE}/
}

do_compile() {

    cd ${S}
	ninja -C ${@get_out_dir(d)} -v
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    cd ${S}/${@get_out_dir(d)}

    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}${includedir}
    install -d ${D}${datadir}/flutter/engine/flutter_patched_sdk

    install -m 644 icudtl.dat ${D}${bindir}
    install -m 755 libflutter_engine.so ${D}${libdir}
    install -m 644 flutter_embedder.h ${D}${includedir}
#    install -m 755 clang_x64/gen_snapshot ${D}${bindir}
    cp -rv flutter_patched_sdk  ${D}${datadir}/flutter/engine
}

FILES_${PN} = " \
    ${bindir}/icudtl.dat \
    ${libdir}/libflutter_engine.so \
    "

FILES_${PN}-dev = " \
    ${includedir}/flutter_embedder.h \
    ${datadir}/flutter/engine/flutter_patched_sdk/* \
    "

INSANE_SKIP_${PN} += "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
