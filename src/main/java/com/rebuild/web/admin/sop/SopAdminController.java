/*!
Copyright (c) REBUILD <https://getrebuild.com/> and/or its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.web.admin.sop;

import com.rebuild.core.support.i18n.Language;
import com.rebuild.utils.RbAssert;
import com.rebuild.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author devezhao
 * @since 2024/4/8
 */
@Controller
public class SopAdminController extends BaseController {

    @GetMapping("/admin/robot/sops")
    public ModelAndView pageList() {
        RbAssert.isCommercial(
                Language.L("免费版不支持业务进度功能 [(查看详情)](https://getrebuild.com/docs/rbv-features)"));
        return createModelAndView("/admin/robot/sop-list");
    }
}
