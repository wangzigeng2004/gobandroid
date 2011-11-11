/**
 * gobandroid 
 * by Marcus -Ligi- Bueschleb 
 * http://ligi.de
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation; 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 **/

package org.ligi.gobandroid_hd.ui;

import java.util.List;


import org.ligi.android.common.dialogs.DialogDiscarder;
import org.ligi.gobandroid_hd.R;
import org.ligi.gobandroid_hd.logic.GnuGoMover;
import org.ligi.gobandroid_hd.logic.GoGame;
import org.ligi.gobandroid_hd.logic.GoGameProvider;
import org.ligi.gobandroid_hd.ui.recording.GameRecordActivity;
import org.ligi.tracedroid.logging.Log;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


/**
 * Activity for a Game
 * 
 * @author <a href="http://ligi.de">Marcus -Ligi- Bueschleb</a>
 * 
 *         This software is licensed with GPLv3
 * 
 **/

public class GoSetupActivity extends FragmentActivity implements OnSeekBarChangeListener, OnClickListener, OnItemSelectedListener{

	private byte act_size=9;
	private byte act_handicap=0;
	
	private final static int size_offset=2;
	 
	private SeekBar size_seek;
	private SeekBar handicap_seek;
	
	private TextView size_text;
	private Button size_button9x9;
	private Button size_button13x13;
	private Button size_button19x19;
	
	private Spinner black_player_spinner;
	private Spinner white_player_spinner;

	private GoBoardViewHD board;
	
	private TextView handicap_text;
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentServices(intent,PackageManager.GET_SERVICES);
	                    
	    return list.size() > 0;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GoPrefs.init(this);
		
		this.setContentView(R.layout.game_setup);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		board=(GoBoardViewHD)this.findViewById(R.id.go_board);
		Log.i("GoSetup Resume");
		size_seek=(SeekBar)this.findViewById(R.id.size_slider);
		size_seek.setOnSeekBarChangeListener(this);
		
		size_text=(TextView)this.findViewById(R.id.game_size_label);
		
		size_button9x9=(Button)this.findViewById(R.id.size_button9x9);
		size_button9x9.setOnClickListener(this);


		((Button)this.findViewById(R.id.InstallAIButton)).setOnClickListener(this);
		
		size_button13x13=(Button)this.findViewById(R.id.size_button13x13);
		size_button13x13.setOnClickListener(this);

		
		size_button19x19=(Button)this.findViewById(R.id.size_button19x19);
		size_button19x19.setOnClickListener(this);
		
		
		black_player_spinner=(Spinner)this.findViewById(R.id.BlackPlayerSpinner);
	
		String[] player_strings;
		if (isIntentAvailable(this,GnuGoMover.intent_action_name))
			player_strings=new String[] {"Phone","GnuGo"};
		else
			player_strings=new String[] {"Phone"};
		
		ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this,
	            android.R.layout.simple_spinner_item , player_strings);

 		spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

 		white_player_spinner=(Spinner)this.findViewById(R.id.WhitePlayerSpinner);
 		
		black_player_spinner.setAdapter(spinner_adapter);
		white_player_spinner.setAdapter(spinner_adapter);
		
		white_player_spinner.setOnItemSelectedListener(this);
		black_player_spinner.setOnItemSelectedListener(this);
		
		// set the default selection 
		int spinner_pos=spinner_adapter.getPosition(GoPrefs.getLastPlayerBlack());
		if (spinner_pos==-1)
			spinner_pos=0;
				
		black_player_spinner.setSelection(spinner_pos,true);
		
		spinner_pos=spinner_adapter.getPosition(GoPrefs.getLastPlayerWhite());
			if (spinner_pos==-1)
				spinner_pos=0;
					
		white_player_spinner.setSelection(spinner_pos,true);

		handicap_text=(TextView)this.findViewById(R.id.handicap_label);
		handicap_seek=(SeekBar)this.findViewById(R.id.handicap_seek);
		handicap_seek.setOnSeekBarChangeListener(this);
		
		// set defaults
		act_size=(byte)GoPrefs.getLastBoardSize();
		act_handicap=(byte)GoPrefs.getLastHandicap();
		
		refresh_ui();
	}
	
	
	/**
	 * refresh the ui elements with values from act_size / act_handicap 
	 */
	public void refresh_ui() {
		size_text.setText("Size "+act_size+"x"+act_size);
		handicap_text.setText("Handicap " + act_handicap);
	
		// the checks for change here are important - otherwise samsung moment will die here with stack overflow
		if ((act_size-size_offset)!=size_seek.getProgress())
			size_seek.setProgress(act_size-size_offset);
		if (act_handicap!=handicap_seek.getProgress())
			handicap_seek.setProgress(act_handicap);
		
		// only enable handicap seeker when the size is 9x9 or 13x13 or 19x19
		handicap_seek.setEnabled((act_size==9)||(act_size==13)||(act_size==19));
		
		GoPrefs.setLastBoardSize(act_size);
		GoPrefs.setLastHandicap(act_handicap);
		
		GoGameProvider.setGame(new GoGame(act_size,act_handicap));
		board.boardSizeChanged();
		board.invalidate();
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if ((seekBar==size_seek)&&(act_size!=(byte)(progress+size_offset))) 
			act_size=(byte)(progress+size_offset);
		else if ((seekBar==handicap_seek)&&(act_handicap!=(byte)progress))
			act_handicap=(byte)progress;

		refresh_ui();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	@Override
	public void onClick(View v) {
		if (v==((Button)this.findViewById(R.id.InstallAIButton))) {
			try {
				this.startActivity(new Intent().setAction(Intent.ACTION_VIEW)
						.setData(Uri.parse("market://search?q=pname:org.ligi.gobandroid.ai.gnugo")));
			}
			catch (Exception e) {
				new AlertDialog.Builder(this).setTitle(R.string.problem)
				.setMessage(
					R.string.android_market_problem
				).setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
			
					}
				}).setCancelable(true).show();
			}
		}
		else 
			if (v==size_button9x9)
				act_size=9;
			if (v==size_button13x13)
				act_size=13;
			else if (v==size_button19x19)
				act_size=19;
		
		refresh_ui();
	}
	
	private void start_game() {
		GoGame new_game=new GoGame(act_size,act_handicap);
		
		GoGameProvider.setGame(new_game);
		Intent go_intent=new Intent(this,GameRecordActivity.class);
		/*go_intent.putExtra("size",act_size );
		go_intent.putExtra("handicap",act_handicap );

		go_intent.putExtra("white_player",white_player_spinner.getSelectedItemPosition());
		go_intent.putExtra("black_player",black_player_spinner.getSelectedItemPosition());
		 */
		
		startActivity(go_intent);
	}

	@Override
	public void onItemSelected(AdapterView<?> selected_item, View selected_view, int pos,
			long arg3) {
		if (selected_item==white_player_spinner)
			GoPrefs.setLastPlayerWhite((String)selected_item.getAdapter().getItem(pos));
		if (selected_item==black_player_spinner)
			GoPrefs.setLastPlayerBlack((String)selected_item.getAdapter().getItem(pos));

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.game_setup, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i("item sel");
		switch (item.getItemId()) {
		case R.id.menu_start:
			/*
			if ((act_size>19)&&((black_player_spinner.getSelectedItemPosition()!=0)||(white_player_spinner.getSelectedItemPosition()!=0)))
					{
						new AlertDialog.Builder(this).setTitle(R.string.problem)
						.setMessage(
    					 R.string.gnugo_size_problem
						).setPositiveButton(R.string.ok,  new DialogDiscarder()).setCancelable(true).show();
    			
					}
			else  */
				 start_game() ;

			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
}