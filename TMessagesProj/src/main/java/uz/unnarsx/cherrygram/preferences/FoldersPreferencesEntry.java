package uz.unnarsx.cherrygram.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.CherrygramConfig;
import uz.unnarsx.cherrygram.helpers.ui.PopupHelper;
import uz.unnarsx.cherrygram.preferences.cells.FoldersPreviewCell;

public class FoldersPreferencesEntry extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int foldersHeaderRow;

    private int foldersPreviewRow;
    private int folderNameAppHeaderRow;
    private int hideAllChatsTabRow;
    private int hideCounterRow;

    private int tabIconTypeRow;
    private int tabStyleRow;
    private int addStrokeRow;

    private int divisorRow;

    protected Theme.ResourcesProvider resourcesProvider;
    protected FoldersPreviewCell foldersPreviewCell;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SWITCH = 1;
    private static final int VIEW_TYPE_TEXT_SETTING = 2;
    private static final int VIEW_TYPE_PREVIEW = 3;
    private static final int VIEW_TYPE_SHADOW = 4;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsId(true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    protected boolean hasWhiteActionBar() {
        return true;
    }

    @Override
    public boolean isLightStatusBar() {
        if (!hasWhiteActionBar()) return super.isLightStatusBar();
        int color = getThemedColor(Theme.key_windowBackgroundWhite);
        return ColorUtils.calculateLuminance(color) > 0.7f;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonDrawable(new BackDrawable(false));

        actionBar.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarWhiteSelector), false);
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setTitleColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);

        actionBar.setTitle(LocaleController.getString("CP_Filters_Header", R.string.CP_Filters_Header));
        actionBar.setAllowOverlayTitle(false);

        actionBar.setOccupyStatusBar(!AndroidUtilities.isTablet());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == folderNameAppHeaderRow) {
                CherrygramConfig.INSTANCE.toggleFolderNameInHeader();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getFolderNameInHeader());
                }
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (position == hideAllChatsTabRow) {
                CherrygramConfig.INSTANCE.toggleTabsHideAllChats();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getTabsHideAllChats());
                }
                foldersPreviewCell.updateAllChatsTabName(true);
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            } else if (position == hideCounterRow) {
                CherrygramConfig.INSTANCE.toggleTabsNoUnread();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getTabsNoUnread());
                }
                foldersPreviewCell.updateTabCounter(true);
                parentLayout.rebuildAllFragmentViews(false, false);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else if (position == tabIconTypeRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();

                arrayList.add(LocaleController.getString("CG_FoldersTypeIconsTitles", R.string.CG_FoldersTypeIconsTitles));
                types.add(CherrygramConfig.TAB_TYPE_MIX);
                arrayList.add(LocaleController.getString("CG_FoldersTypeTitles", R.string.CG_FoldersTypeTitles));
                types.add(CherrygramConfig.TAB_TYPE_TEXT);
                arrayList.add(LocaleController.getString("CG_FoldersTypeIcons", R.string.CG_FoldersTypeIcons));
                types.add(CherrygramConfig.TAB_TYPE_ICON);

                PopupHelper.show(arrayList, LocaleController.getString("CG_FoldersType_Header", R.string.CG_FoldersType_Header), types.indexOf(CherrygramConfig.INSTANCE.getTabMode()), context, i -> {
                    CherrygramConfig.INSTANCE.setTabMode(types.get(i));

                    foldersPreviewCell.updateTabIcons(true);
                    foldersPreviewCell.updateTabTitle(true);
                    listAdapter.notifyItemChanged(tabIconTypeRow);
                    parentLayout.rebuildAllFragmentViews(false, false);
                    getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                });
            } else if (position == tabStyleRow) {
                ArrayList<String> arrayList = new ArrayList<>();
                ArrayList<Integer> types = new ArrayList<>();

                arrayList.add(LocaleController.getString("AP_Tab_Style_Default", R.string.AP_Tab_Style_Default));
                types.add(CherrygramConfig.TAB_STYLE_DEFAULT);
                arrayList.add(LocaleController.getString("AP_Tab_Style_Rounded", R.string.AP_Tab_Style_Rounded));
                types.add(CherrygramConfig.TAB_STYLE_ROUNDED);
                arrayList.add(LocaleController.getString("AP_Tab_Style_Text", R.string.AP_Tab_Style_Text));
                types.add(CherrygramConfig.TAB_STYLE_TEXT);
                arrayList.add("VKUI");
                types.add(CherrygramConfig.TAB_STYLE_VKUI);
                arrayList.add(LocaleController.getString("AP_Tab_Style_Pills", R.string.AP_Tab_Style_Pills));
                types.add(CherrygramConfig.TAB_STYLE_PILLS);

                PopupHelper.show(arrayList, LocaleController.getString("AP_Tab_Style", R.string.AP_Tab_Style), types.indexOf(CherrygramConfig.INSTANCE.getTabStyle()), context, i -> {
                    CherrygramConfig.INSTANCE.setTabStyle(types.get(i));

                    foldersPreviewCell.updateTabStyle(true);
                    listAdapter.notifyItemChanged(tabStyleRow);
                    parentLayout.rebuildAllFragmentViews(false, false);
                    getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    updateRowsId(false);
                });
            } else if (position == addStrokeRow) {
                CherrygramConfig.INSTANCE.toggleTabStyleStroke();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(CherrygramConfig.INSTANCE.getTabStyleStroke());
                }
                foldersPreviewCell.updateTabStroke(true);
                parentLayout.rebuildAllFragmentViews(false, false);
            }
        });

        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId(boolean notify) {
        rowCount = 0;

        foldersHeaderRow = rowCount++;

        foldersPreviewRow = rowCount++;
        divisorRow = rowCount++;

        folderNameAppHeaderRow = rowCount++;
        hideAllChatsTabRow = rowCount++;
        hideCounterRow = rowCount++;
        tabIconTypeRow = rowCount++;
        tabStyleRow = rowCount++;

        int prevAddStrokeRow = addStrokeRow;
        addStrokeRow = -1;
        if (CherrygramConfig.INSTANCE.getTabStyle() >= CherrygramConfig.TAB_STYLE_VKUI) addStrokeRow = rowCount++;
        if (listAdapter != null) {
            if (prevAddStrokeRow == -1 && addStrokeRow != -1) {
                listAdapter.notifyItemInserted(addStrokeRow);
            } else if (prevAddStrokeRow != -1 && addStrokeRow == -1) {
                listAdapter.notifyItemRemoved(prevAddStrokeRow);
            }
        }

        divisorRow = rowCount++;

        if (listAdapter != null && notify) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == foldersHeaderRow) {
                        headerCell.setText(LocaleController.getString("CallVideoPreviewTitle", R.string.CallVideoPreviewTitle));
                    }
                    break;
                case VIEW_TYPE_SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    textCheckCell.setEnabled(true, null);
                    if (position == folderNameAppHeaderRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("AP_FolderNameInHeader", R.string.AP_FolderNameInHeader), LocaleController.getString("AP_FolderNameInHeader_Desc", R.string.AP_FolderNameInHeader_Desc), CherrygramConfig.INSTANCE.getFolderNameInHeader(), true, true);
                    } else if (position == hideAllChatsTabRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CP_NewTabs_RemoveAllChats", R.string.CP_NewTabs_RemoveAllChats), CherrygramConfig.INSTANCE.getTabsHideAllChats(), true);
                    } else if (position == hideCounterRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("CP_NewTabs_NoCounter", R.string.CP_NewTabs_NoCounter), CherrygramConfig.INSTANCE.getTabsNoUnread(), true);
                    } else if (position == addStrokeRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AP_Tab_Style_Stroke", R.string.AP_Tab_Style_Stroke), CherrygramConfig.INSTANCE.getTabStyleStroke(), true);
                    }
                    break;
                case VIEW_TYPE_TEXT_SETTING:
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == tabIconTypeRow) {
                        String value;
                        switch (CherrygramConfig.INSTANCE.getTabMode()) {
                            case CherrygramConfig.TAB_TYPE_MIX:
                                value = LocaleController.getString("CG_FoldersTypeIconsTitles", R.string.CG_FoldersTypeIconsTitles);
                                break;
                            case CherrygramConfig.TAB_TYPE_ICON:
                                value = LocaleController.getString("CG_FoldersTypeIcons", R.string.CG_FoldersTypeIcons);
                                break;
                            default:
                            case CherrygramConfig.TAB_TYPE_TEXT:
                                value = LocaleController.getString("CG_FoldersTypeTitles", R.string.CG_FoldersTypeTitles);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("CG_FoldersType_Header", R.string.CG_FoldersType_Header), value, true);
                    } else if (position == tabStyleRow) {
                        String value;
                        switch (CherrygramConfig.INSTANCE.getTabStyle()) {
                            case CherrygramConfig.TAB_STYLE_DEFAULT:
                                value = LocaleController.getString("AP_Tab_Style_Default", R.string.AP_Tab_Style_Default);
                                break;
                            case CherrygramConfig.TAB_STYLE_TEXT:
                                value = LocaleController.getString("AP_Tab_Style_Text", R.string.AP_Tab_Style_Text);
                                break;
                            case CherrygramConfig.TAB_STYLE_VKUI:
                                value = "VKUI";
                                break;
                            case CherrygramConfig.TAB_STYLE_PILLS:
                                value = LocaleController.getString("AP_Tab_Style_Pills", R.string.AP_Tab_Style_Pills);
                                break;
                            default:
                            case CherrygramConfig.TAB_STYLE_ROUNDED:
                                value = LocaleController.getString("AP_Tab_Style_Rounded", R.string.AP_Tab_Style_Rounded);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("AP_Tab_Style", R.string.AP_Tab_Style), value, true);
                    }
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_SWITCH || type == VIEW_TYPE_TEXT_SETTING;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SWITCH:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_TEXT_SETTING:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_PREVIEW:
                    foldersPreviewCell = new FoldersPreviewCell(mContext);
                    foldersPreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                    return new RecyclerListView.Holder(foldersPreviewCell);
                case VIEW_TYPE_SHADOW:
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == foldersHeaderRow) {
                return VIEW_TYPE_HEADER;
            } else if (position == folderNameAppHeaderRow || position == hideAllChatsTabRow || position == hideCounterRow || position == addStrokeRow) {
                return VIEW_TYPE_SWITCH;
            } else if (position == tabIconTypeRow || position == tabStyleRow) {
                return VIEW_TYPE_TEXT_SETTING;
            } else if (position == foldersPreviewRow) {
                return VIEW_TYPE_PREVIEW;
            }
            return VIEW_TYPE_SHADOW;
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
