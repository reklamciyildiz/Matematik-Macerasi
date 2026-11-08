package com.yigit033.matematikmacarasi.services

/** A stored signed license cannot be read in a legacy-only APK.
 * Never use a locally editable Boolean as a replacement for RSA verification.
 */
object PremiumCodeBuildPolicy {
    fun isIncompatible(cloudflareEnabled:Boolean,signedLicensePresent:Boolean):Boolean =
        !cloudflareEnabled && signedLicensePresent
}
