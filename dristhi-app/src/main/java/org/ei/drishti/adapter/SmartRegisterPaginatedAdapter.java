package org.ei.drishti.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import org.ei.drishti.provider.SmartRegisterClientsProvider;
import org.ei.drishti.view.contract.ECClient;
import org.ei.drishti.view.contract.SmartRegisterClients;
import org.ei.drishti.view.dialog.FilterOption;
import org.ei.drishti.view.dialog.ServiceModeOption;
import org.ei.drishti.view.dialog.SortOption;

public class SmartRegisterPaginatedAdapter extends BaseAdapter {
    private int clientCount;
    private int pageCount;
    private int currentPage = 0;
    protected static final int CLIENTS_PER_PAGE = 20;

    protected final Context context;

    private SmartRegisterClients filteredClients;
    protected final SmartRegisterClientsProvider listItemProvider;

    public SmartRegisterPaginatedAdapter(Context context,
                                         SmartRegisterClientsProvider listItemProvider) {
        this.context = context;
        this.listItemProvider = listItemProvider;
        refreshClients(listItemProvider.getClients());
    }

    protected void refreshClients(SmartRegisterClients filteredClients) {
        this.filteredClients = filteredClients;
        clientCount = filteredClients.size();
        pageCount = (int) Math.ceil((double)clientCount / (double)CLIENTS_PER_PAGE);
        currentPage = 0;
    }

    @Override
    public int getCount() {
        if (clientCount <= CLIENTS_PER_PAGE) {
            return clientCount;
        } else if (currentPage == pageCount() - 1) {
            return clientCount % CLIENTS_PER_PAGE;
        }
        return CLIENTS_PER_PAGE;
    }

    @Override
    public Object getItem(int i) {
        return filteredClients.get(i);
    }

    @Override
    public long getItemId(int i) {
        return actualPosition(i);
    }

    @Override
    public View getView(int i, View parentView, ViewGroup viewGroup) {
        return listItemProvider.getView((ECClient) getItem(actualPosition(i)), parentView, viewGroup);
    }

    private int actualPosition(int i) {
        if (clientCount <= CLIENTS_PER_PAGE) {
            return i;
        } else {
            return i + (currentPage * CLIENTS_PER_PAGE);
        }
    }

    public int pageCount() {
        return pageCount;
    }

    public int currentPage() {
        return currentPage;
    }

    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
        }
    }

    public boolean hasNextPage() {
        return currentPage < pageCount() - 1;
    }

    public boolean hasPreviousPage() {
        return currentPage > 0;
    }

    public void refreshList(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                            FilterOption searchFilter, SortOption sortOption) {
        SmartRegisterClients filteredClients = listItemProvider
                .updateClients(villageFilter, serviceModeOption, searchFilter, sortOption);
        refreshClients(filteredClients);
        notifyDataSetChanged();
    }
}
