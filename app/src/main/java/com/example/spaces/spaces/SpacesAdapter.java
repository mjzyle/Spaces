package com.example.spaces.spaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.graphics.Color;
import java.util.ArrayList;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.spaces.spaces.models.StudyLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;


/**
 * Created by Steven on 3/14/2018.
 */

public class SpacesAdapter extends RecyclerView.Adapter<SpacesAdapter.ViewHolder> {
    private static final String TAG = "SpacesAdapter";

    private Context context;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private StudyLocation[] locations;
    private ViewHolder[] viewHolders;

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CardView mCardView;       // Containing card for space
        public TextView spaceName;       // Space name
        public ImageView spaceImage;     // Space thumbnail
        public TextView spaceRating;     // Overall space rating
        public StudyLocation currentLocation;

        public ViewHolder(View v) {
            super(v);
            spaceName = v.findViewById(R.id.spaceName);
            mCardView = v.findViewById(R.id.cardView);
            spaceRating = v.findViewById(R.id.spaceRating);
            spaceImage = v.findViewById(R.id.spaceImage);

            // Select an individual space and retrieve the space page
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), SpacePageActivity.class);
                    // open the space page for the location in this viewholder
                    v.getContext().startActivity(
                            i.putExtra("name", spaceName.getText().toString())
                    );
                }
            });
        }
    }

    /**
     *  Provide a suitable constructor (depends on the kind of dataset)
     */
    public SpacesAdapter(Context context, StudyLocation[] locations) {
        // [START get_storage_ref]
        this.mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
        // [START get_database_ref]
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END get_database_ref]
        this.context = context;
        this.locations = locations;
        this.viewHolders = new ViewHolder[locations.length];
    }

    // Context-independent constructor for testing
    public SpacesAdapter(StudyLocation[] locations) {
        this.locations = locations;
        this.viewHolders = new ViewHolder[locations.length];
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    @Override
    public SpacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spaces_cardview_item, parent, false);

        return new ViewHolder(v);
    }

    /**
     *  Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        viewHolders[position] = holder;

        final String locationName = locations[position].getLocationName();
        final DatabaseReference locationRef = mDatabase.child("locations").child(locationName);

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // pull location from database into a StudyLocation
                locations[position] = new StudyLocation(locationName, dataSnapshot);
                Log.d(TAG, locationName+" got images for viewholder"+locations[position].getPictureIds());
                // replace the contents of the view with that of the StudyLocation
                setupLocationCard(locationName, position);
            }
            @Override
            public void onCancelled(DatabaseError de) {
                de.toException().printStackTrace();
            }
        });

    }

    private void setupLocationCard(final String locationName, final int position) {
        // Set card characteristics
        if (position % 2 == 0) {
            viewHolders[position].mCardView.setCardBackgroundColor(Color.parseColor("#cfd8dc"));
            viewHolders[position].spaceName.setBackgroundColor(Color.parseColor("#cfd8dc"));
        }
        else {   // Alternate colors for enhanced readability
            viewHolders[position].mCardView.setCardBackgroundColor(Color.parseColor("#efebe9"));
            viewHolders[position].spaceName.setBackgroundColor(Color.parseColor("#efebe9"));
        }

        // set space name
        viewHolders[position].spaceName.setText(locationName);
        viewHolders[position].spaceName.setTextSize(24);

        // set thumbnail image
        Map<String, String> picIds = null;
        if (locations[position].getPictureIds() != null) {
            picIds = locations[position].getPictureIds();
            System.out.println(locationName+": found pictures " +picIds);
        } else System.out.println(locationName + ": had null picture ids");

        if (picIds != null && !picIds.isEmpty()) {
            Object[] ids = picIds.values().toArray();
            // choose the last image (this is arbitrary)
            String id = ids[ids.length-1].toString();
            System.out.println(locationName + ": setting thumbnail");
            // set an image from this location as the thumbnail
            String path = ImageUploader.getImagePath(Uri.parse(id));
            System.out.println("got path "+path);

            setThumbnail(position, mStorageRef.child(path), Uri.parse(id));
        }

        // set space rating
        StringBuilder s = new StringBuilder();   // Display rating to one decimal
        String rating = Double.toString(locations[position].getOverallReviewAvg());
        for (int i = 0; i < 3; i++) {
            s.append(rating.charAt(i));
        }
        rating = s.toString();
        viewHolders[position].spaceRating.setText(rating);
        int RatingColor;               // Set text color based on rating value
        if (locations[position].getOverallReviewAvg() < 2)
            RatingColor = Color.RED;   // 0-1 rating is red
        else if (locations[position].getOverallReviewAvg() < 4)
            RatingColor = Color.parseColor("#a5ad32");  // 2-4 rating is yellow
        else
            RatingColor = Color.parseColor("#5d9624");   // 4+ rating is green
        viewHolders[position].spaceRating.setTextSize(18);
        viewHolders[position].spaceRating.setTextColor(RatingColor);

        // Determine the current page for responding to card selections
        setCurrentLocation(locations[position], viewHolders[position]);
    }

    private void setCurrentLocation(StudyLocation l, ViewHolder h) {
        h.currentLocation = l;
    }

    private void setThumbnail(final int position, final StorageReference imgRef, final Uri uri) {

        RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
            Handler handler = new Handler();
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                // try again in case there is a local version that hasn't finished uploading
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getLocalCopy();
                    }
                    private void getLocalCopy() {
                        Glide.with(context)
                                .load(uri)
                                .into(viewHolders[position].spaceImage);
                    }
                });
                // return false so the error placeholder can be placed
                return false;
            }
            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                // everything worked out, so probably nothing to do
                return false;
            }
        };

        // load the image from firebase storage
        Glide.with(context)
                .load(imgRef)
                .listener(requestListener)
                .into(viewHolders[position].spaceImage);

    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return locations.length;
    }

    // Return unit holders for testing purposes
    public ViewHolder[] getHolders() {
        return viewHolders;
    }
}
