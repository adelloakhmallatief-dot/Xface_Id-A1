package com.xcoder.xfaceid.registration

import com.xcoder.xfaceid.R
import com.xcoder.xfaceid.core.ai.detection.QualityIssue

/** Teks arahan untuk pengguna; dipakai bersama oleh UI Standard dan Lite. */
fun issueMessageRes(issue: QualityIssue?): Int = when (issue) {
    null -> R.string.issue_hold_still
    QualityIssue.NO_FACE -> R.string.issue_no_face
    QualityIssue.MULTIPLE_FACES -> R.string.issue_multiple_faces
    QualityIssue.LOW_CONFIDENCE -> R.string.issue_low_confidence
    QualityIssue.TOO_FAR -> R.string.issue_too_far
    QualityIssue.TOO_CLOSE -> R.string.issue_too_close
    QualityIssue.OFF_CENTER -> R.string.issue_off_center
    QualityIssue.NOT_FRONTAL -> R.string.issue_not_frontal
}
