/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 */

package com.eup.codeopsstudio.util;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.customview.widget.Openable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import com.google.android.material.navigation.NavigationView;

import java.util.Map.Entry;

/**
 * @author Etido Peter
 */
public class NavigationManager {
    private final MorphMap<Integer, Integer> pairedIds;
    private final Openable openableLayout;
    private final NavigationView navigationView;
    private final NavController navController;

    protected NavigationManager(@NonNull MorphMap<Integer, Integer> idPairs,
        @NonNull Openable openable, @NonNull NavigationView navigationView,
        @NonNull NavController navController) {
        this.pairedIds      = idPairs;
        this.openableLayout = openable;
        this.navigationView = navigationView;
        this.navController  = navController;
    }

    public NavController getNavController() {
        return navController;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public Openable getOpenableLayout() {
        return openableLayout;
    }

    public MorphMap<Integer, Integer> getPairedIds() {
        return pairedIds;
    }

    public static class Builder {

        private MorphMap<Integer, Integer> morphedIds;
        private Openable openableLayout;
        private NavigationView navigationView;
        private NavController navController;
        private boolean autoCloseOpenable;
        private int checkedMenuItemId = -1;
        private MenuItem checkedMenuItem;
        private SecondOrderNavigation secondOrderNavigation;

        public Builder() { }

        public Builder autoCloseOpenable(boolean autoClose) {
            this.autoCloseOpenable = autoClose;
            return this;
        }

        public NavigationManager build() {
            DrawerLayout drawer = getDrawer();

            if (checkedMenuItem != null) {
                navigationView.setCheckedItem(checkedMenuItem);
            }

            if (checkedMenuItemId != -1) {
                navigationView.setCheckedItem(checkedMenuItemId);
            }

            if (checkedMenuItem == null && checkedMenuItemId == -1) {
                navigationView.setCheckedItem(navigationView.getMenu().getItem(0));
            }

            MenuItem checkedItem = navigationView.getCheckedItem();

            if (checkedItem != null) {
                checkedItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            navigationView.setNavigationItemSelectedListener(menuItem -> {
                navigationView.setCheckedItem(menuItem);

                boolean autoClose = true;
                if (navController != null) {
                    if (autoCloseOpenable && drawer != null) {
                        drawer.closeDrawer(navigationView);
                    }
                    return onNavDestinationSelected(menuItem.getItemId());
                }
                return false;
            });

            return new NavigationManager(morphedIds, openableLayout, navigationView, navController);
        }

        public DrawerLayout getDrawer() {
            return (openableLayout != null && openableLayout instanceof DrawerLayout)
                ? (DrawerLayout) openableLayout : null;
        }

        private boolean onNavDestinationSelected(final int itemId) {
            for (Entry<Integer, Integer> entry : morphedIds.entrySet()) {
                int navId = entry.getKey();
                int menuId = entry.getValue();

                if (menuId == itemId && menuId == navId) {
                    return navigateToFragment(navId);
                } else if (!morphedIds.containsKey(itemId)) {
                    if (secondOrderNavigation != null) {
                        return secondOrderNavigation.onNavDestinationSelected(itemId);
                    }
                }
            }
            return false;
        }

        /**
         * Navigates to menu associated with a fragment
         *
         * @return true Since fragments must be made selectable
         */
        private boolean navigateToFragment(int fragmentId) {
            if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() != fragmentId) {
                navController.navigate(fragmentId); // Assuming the fragment is not displayed we
                // add it
            }
            return true;
        }

        public Builder setCheckedMenuItem(MenuItem item) {
            this.checkedMenuItem = item;
            return this;
        }

        public Builder setCheckedMenuItem(int id) {
            this.checkedMenuItemId = id;
            return this;
        }

        public Builder setNavController(NavController navController) {
            this.navController = navController;
            return this;
        }

        public Builder setNavigationView(NavigationView navigationView) {
            this.navigationView = navigationView;
            return this;
        }

        public Builder setOpenable(Openable openable) {
            this.openableLayout = openable;
            return this;
        }

        public Builder setSecondOrderNavigationListener(SecondOrderNavigation navigation) {
            this.secondOrderNavigation = navigation;
            return this;
        }

        /**
         * Parses each ID as a set of Ids because each menu should be considered as top level
         * destinations.
         *
         * @param ids the array of menu ids which are same as the navigation ids
         */
        public Builder setTopLevelIds(Object... ids) {
            return setNavAndMenuIds(MorphMap.duplicateOf(ids));
        }

        public Builder setNavAndMenuIds(MorphMap<Integer, Integer> ids) {
            this.morphedIds = ids;
            return this;
        }

        public interface SecondOrderNavigation {
            boolean onNavDestinationSelected(final int itemId);
        }
    }
}
