package javax.activation;

import java.io.IOException;
import java.io.OutputStream;
import myjava.awt.datatransfer.DataFlavor;
import myjava.awt.datatransfer.UnsupportedFlavorException;

/* compiled from: DataHandler */
class DataSourceDataContentHandler implements DataContentHandler {
    private DataContentHandler dch = null;

    /* renamed from: ds */
    private DataSource f21ds = null;
    private DataFlavor[] transferFlavors = null;

    public DataSourceDataContentHandler(DataContentHandler dch2, DataSource ds) {
        this.f21ds = ds;
        this.dch = dch2;
    }

    public DataFlavor[] getTransferDataFlavors() {
        if (this.transferFlavors == null) {
            if (this.dch != null) {
                this.transferFlavors = this.dch.getTransferDataFlavors();
            } else {
                this.transferFlavors = new DataFlavor[1];
                this.transferFlavors[0] = new ActivationDataFlavor(this.f21ds.getContentType(), this.f21ds.getContentType());
            }
        }
        return this.transferFlavors;
    }

    public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException {
        if (this.dch != null) {
            return this.dch.getTransferData(df, ds);
        }
        if (df.equals(getTransferDataFlavors()[0])) {
            return ds.getInputStream();
        }
        throw new UnsupportedFlavorException(df);
    }

    public Object getContent(DataSource ds) throws IOException {
        if (this.dch != null) {
            return this.dch.getContent(ds);
        }
        return ds.getInputStream();
    }

    public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
        if (this.dch != null) {
            this.dch.writeTo(obj, mimeType, os);
            return;
        }
        throw new UnsupportedDataTypeException("no DCH for content type " + this.f21ds.getContentType());
    }
}
