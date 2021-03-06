package serializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;

import serializers.avro.media.Image;
import serializers.avro.media.Media;
import serializers.avro.media.MediaContent;
import data.media.MediaTransformer;

/**
 * Transformer is needed when we use Avro-generated Java classes, to convert
 * between these and POJOs for verification purposes. This transformation
 * is NOT done during performance testing, as it could be viewed as
 * unfair overhead for use cases where generated class is used instead
 * of equivalent POJO.
 */
public class AvroTransformer extends MediaTransformer<MediaContent>
{
    @Override
    public MediaContent[] resultArray(int size) { return new MediaContent[size]; }

    // ----------------------------------------------------------
    // Forward

    public MediaContent forward(data.media.MediaContent mc)
    {
        GenericArray<Image> images = new GenericData.Array<Image>(mc.images.size(), Avro.Media.sImages);
        for (data.media.Image image : mc.images) {
            images.add(forwardImage(image));
        }

        MediaContent amc = new MediaContent();
        amc.media = forwardMedia(mc.media);
        amc.images = images;
        return amc;
    }

    private Media forwardMedia(data.media.Media media)
    {
            Media m = new Media();

            m.uri = media.uri;
            m.title = media.title;
            m.width = media.width;
            m.height = media.height;
            m.format = media.format;
            m.duration = media.duration;
            m.size = media.size;
            if (media.hasBitrate) m.bitrate = media.bitrate;

            GenericArray<CharSequence> persons = new GenericData.Array<CharSequence>(media.persons.size(), Avro.Media.sPersons);
            for (String s : media.persons) {
              persons.add(s);
            }
            m.persons = persons;

            m.player = forwardPlayer(media.player);
            m.copyright = media.copyright;

            return m;
    }

    public int forwardPlayer(data.media.Media.Player p)
    {
            switch (p) {
                    case JAVA: return 1;
                    case FLASH: return 2;
                    default: throw new AssertionError("invalid case: " + p);
            }
    }

    private Image forwardImage(data.media.Image image)
    {
            Image i = new Image();
            i.uri = image.uri;
            i.title = image.title;
            i.width = image.width;
            i.height = image.height;
            i.size = forwardSize(image.size);
            return i;
    }

    public int forwardSize(data.media.Image.Size s)
    {
            switch (s) {
                    case SMALL: return 1;
                    case LARGE: return 2;
                    default: throw new AssertionError("invalid case: " + s);
            }
    }

    // ----------------------------------------------------------
    // Reverse

    public data.media.MediaContent reverse(MediaContent mc)
    {
            List<data.media.Image> images = new ArrayList<data.media.Image>((int) mc.images.size());

            for (Image image : mc.images) {
                    images.add(reverseImage(image));
            }

            return new data.media.MediaContent(reverseMedia(mc.media), images);
    }

    private data.media.Media reverseMedia(Media media)
    {
            // Media
            List<String> persons = new ArrayList<String>();
            for (CharSequence p : media.persons) {
                    persons.add(p.toString());
            }

            return new data.media.Media(
                    media.uri.toString(),
                    media.title != null ? media.title.toString() : null,
                    media.width,
                    media.height,
                    media.format.toString(),
                    media.duration,
                    media.size,
                    media.bitrate != null ? media.bitrate : 0,
                    media.bitrate != null,
                    persons,
                    reversePlayer(media.player),
                    media.copyright != null ? media.copyright.toString() : null
            );
    }

    public data.media.Media.Player reversePlayer(int p)
    {
        switch (p) {
        case 1: return data.media.Media.Player.JAVA;
        case 2: return data.media.Media.Player.FLASH;
        default: throw new AssertionError("invalid case: " + p);
        }
    }

    private data.media.Image reverseImage(Image image)
    {
        return new data.media.Image(
                image.uri.toString(),
                image.title != null ? image.title.toString() : null,
                image.width,
                image.height,
                reverseSize(image.size));
    }

    public data.media.Image.Size reverseSize(int s)
    {
        switch (s) {
        case 1: return data.media.Image.Size.SMALL;
        case 2: return data.media.Image.Size.LARGE;
        default: throw new AssertionError("invalid case: " + s);
        }
    }

    public data.media.MediaContent shallowReverse(MediaContent mc)
    {
        return new data.media.MediaContent(reverseMedia(mc.media), Collections.<data.media.Image>emptyList());
    }
}
