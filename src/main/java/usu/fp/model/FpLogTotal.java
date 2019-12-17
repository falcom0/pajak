package usu.fp.model;

import java.util.List;

public class FpLogTotal {
    private List<FpLog> listPnsHadir;
    private List<FpLog> listNonPnsHadir;
    private List<FpLog> listPnsTdkHadir;
    private List<FpLog> listNonPnsTdkHadir;

    public void setListNonPnsHadir(List<FpLog> listNonPnsHadir) {
        this.listNonPnsHadir = listNonPnsHadir;
    }

    public void setListNonPnsTdkHadir(List<FpLog> listNonPnsTdkHadir) {
        this.listNonPnsTdkHadir = listNonPnsTdkHadir;
    }

    public void setListPnsHadir(List<FpLog> listPnsHadir) {
        this.listPnsHadir = listPnsHadir;
    }

    public void setListPnsTdkHadir(List<FpLog> listPnsTdkHadir) {
        this.listPnsTdkHadir = listPnsTdkHadir;
    }
}
