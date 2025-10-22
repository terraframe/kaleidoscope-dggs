package ai.terraframe.kaleidoscope.dggs.core.model;

public class RemoteDggsException extends RuntimeException
{
  private static final long serialVersionUID = -2287472400338293280L;

  private int               status           = 400;

  public RemoteDggsException()
  {
    super();
  }

  public RemoteDggsException(String message, int status)
  {
    super(message);

    this.status = status;
  }

  public int getStatus()
  {
    return status;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }

}
