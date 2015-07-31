package com.esri.arcgis.android.samples.addcsv2graphic;

/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

import android.content.Context;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

public class OfflineActions implements Callback {

  private static final int MENU_DISCARD = 1;

  Context mContext;

  public OfflineActions(final AddCSVActivity activity) {
    mContext = activity;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_DISCARD:
        ((AddCSVActivity) mContext).clear();
        break;

      default:
        break;
    }
    return false;
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuItem item;
    item = menu.add(Menu.NONE, MENU_DISCARD, 1, "save");
    item.setIcon(R.drawable.ic_action_content_discard);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
      ((AddCSVActivity) mContext).save();
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

}
