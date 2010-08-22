package net.wigle.wigleandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * display latest error stack, if any.
 * allow the stack to be emailed to us.
 * @author bobzilla
 *
 */
public class ErrorReportActivity extends Activity {
  private static final int MENU_EXIT = 11;
  private boolean fromFailure = false;
  
  @Override
  public void onCreate( final Bundle savedInstanceState) {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.error );
    
    // get stack from file
    String stack = getLatestStack();
    
    // set on view
    TextView tv = (TextView) findViewById( R.id.errorreport );
    tv.setText( stack );
    
    Intent intent = getIntent();
    boolean doEmail = intent.getBooleanExtra( WigleAndroid.ERROR_REPORT_DO_EMAIL, false );
    if ( doEmail ) {
      fromFailure = true;
      // setup email sending
      setupEmail( stack );
    }
  }
  
  private String getLatestStack() {
    StringBuilder builder = new StringBuilder();
    try {
      File fileDir = new File( Environment.getExternalStorageDirectory().getCanonicalPath() + "/wiglewifi/" );
      if ( ! fileDir.canRead() || ! fileDir.isDirectory() ) {
        WigleAndroid.error( "file is not readable or not a directory. fileDir: " + fileDir );
      }
      else {
        String[] files = fileDir.list();
        if ( files == null ) {
          WigleAndroid.error( "no files in dir: " + fileDir );
        }
        else {
          String latestFilename = null;
          for ( String filename : files ) {
            if ( filename.startsWith( WigleAndroid.ERROR_STACK_FILENAME ) ) {
              if ( latestFilename == null || filename.compareTo( latestFilename ) > 0 ) {
                latestFilename = filename;
              }
            }
          }
          WigleAndroid.info( "latest filename: " + latestFilename );
          
          String filePath = fileDir.getCanonicalPath() + "/" + latestFilename;
          BufferedReader reader = new BufferedReader( new FileReader( filePath ) );
          String line = reader.readLine();
          while ( line != null ) {
            builder.append( line ).append( "\n" );
            line = reader.readLine();
          }
        }
      }
    }
    catch ( IOException ex ) {
      WigleAndroid.error( "error reading stack file: " + ex, ex );
    }
    
    return builder.toString();
  }
  
  private void setupEmail( String stack ) {
    WigleAndroid.info( "ErrorReport onCreate" );
    final Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );
    emailIntent.setType( "plain/text" );
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"bobzilla@wigle.net"} );
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "WigleWifi error report" );
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stack );
    final Intent chooserIntent = Intent.createChooser( emailIntent, "Email WigleWifi error report?" );
    startActivity( chooserIntent );
  }
  
  /* Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu( final Menu menu ) {
    if ( fromFailure ) {
      MenuItem item = menu.add(0, MENU_EXIT, 0, "Exit");
      item.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
    }
    else {
      MenuItem item = menu.add(0, MENU_EXIT, 0, "Return");
      item.setIcon( android.R.drawable.ic_media_previous );
    }
    
    return true;
  }
  
  /* Handles item selections */
  @Override
  public boolean onOptionsItemSelected( final MenuItem item ) {
      switch ( item.getItemId() ) {
        case MENU_EXIT:
          finish();
          return true;
      }
      return false;
  }
  
}