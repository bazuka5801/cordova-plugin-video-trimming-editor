package plugin.videotrimmingeditor.utils;

import android.net.Uri;
import android.util.Log;

import com.blankj.utilcode.util.UriUtils;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}.
 * @since 3/23/16.
 */
public class VideoUtils {
    public static boolean checkFileSupport(String src) {
        try {
            File input = UriUtils.uri2File(Uri.parse(src));
            if (input == null) {
                input = new File(Uri.parse(src).getPath());
            }

            FileDataSourceImpl file = new FileDataSourceImpl(input);
            Movie movie = MovieCreator.build(file);
            return !movie.getTracks().isEmpty();
        } catch (Exception e) {
            Log.e("checkFileSupport", e.getMessage(), e);
            return false;
        }
    }

    public static void startTrim(File src, File dst, int startMs, int endMs) throws IOException {
        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(file);
        // remove all tracks we will create new tracks from the old
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        double startTime = (double) startMs / 1000;
        double endTime = (double) endMs / 1000;
        boolean timeCorrected = false;
        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 1) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                double startTime1 = correctTimeToSyncSample(track, startTime, false);
                double endTime1 = correctTimeToSyncSample(track, endTime, true);
                if (startTime1 != endTime1 && endTime1 != 0.00) {
                    startTime = startTime1;
                    endTime = endTime1;
                    timeCorrected = true;
                }
            }
        }
        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            long startSample = -1;
            long endSample = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                if (currentTime <= startTime) {

                    // current sample is still before the new starttime
                    startSample = currentSample;
                }
                if (currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample = currentSample;
                } else {
                    // current sample is after the end of the cropped video
                    break;
                }
                currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample, endSample)));
        }

        Container out = new DefaultMp4Builder().build(movie);
        MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
        //mvhd.setMatrix(Matrix.ROTATE_180);
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dst);
        WritableByteChannel fc = fos.getChannel();
        try {
            out.writeContainer(fc);
        } finally {
            fc.close();
            fos.close();
            file.close();
        }

        file.close();
    }


    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}
