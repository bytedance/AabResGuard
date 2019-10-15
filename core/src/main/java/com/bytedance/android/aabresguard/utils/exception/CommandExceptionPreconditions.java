package com.bytedance.android.aabresguard.utils.exception;

import com.android.tools.build.bundletool.flags.Flag;
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException;

import java.util.Optional;

/**
 * Created by YangJing on 2019/10/10 .
 * Email: yangjing.yeoh@bytedance.com
 */
public final class CommandExceptionPreconditions {

    public static void checkFlagPresent(Object object, Flag flag) {
        if (object instanceof Optional) {
            object = ((Optional) object).get();
        }
        Optional.of(object).orElseThrow(() -> CommandExecutionException.builder()
                .withMessage("Wrong properties: %s can not be empty", flag)
                .build());
    }

    public static void checkStringIsEmpty(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("Wrong properties: %s can not be empty", name));
        }
    }
}
