package org.eu.hanana.reimu.ottohub_andriod.util;

import androidx.annotation.IntDef;

import com.tencent.tinker.loader.shareutil.ShareConstants;

@IntDef(flag = true, value = {
        ShareConstants.TINKER_DISABLE,
        ShareConstants.TINKER_ENABLE_ALL,
        ShareConstants.TINKER_RESOURCE_MASK,
        ShareConstants.TINKER_NATIVE_LIBRARY_MASK,
        ShareConstants.TINKER_DEX_AND_LIBRARY,
        ShareConstants.TINKER_ARKHOT_MASK,
        ShareConstants.TINKER_DEX_MASK
})
public @interface TypeTinkerFlag {
}
