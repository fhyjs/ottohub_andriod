package org.eu.hanana.reimu.ottohub_andriod.data.base.text;

import static org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil.throwApiError;

import org.eu.hanana.reimu.lib.ottohub.api.im.MessageListResult;
import org.eu.hanana.reimu.ottohub_andriod.data.base.ListViewModelBase;
import org.eu.hanana.reimu.ottohub_andriod.data.message.MessageCard;
import org.eu.hanana.reimu.ottohub_andriod.ui.base.ListFragmentBase;
import org.eu.hanana.reimu.ottohub_andriod.ui.message.MessageListFragment;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class TextViewModel extends ListViewModelBase<TextCard> {
}
