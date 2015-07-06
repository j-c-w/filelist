This is a project that is aimed at storing lists of objects that would normally cause OutOfMemoryExceptions. (Most importantly, bitmaps).

This works by using a temporary folder on the device to write the objects to when they are not needed in memory. It maintains a pointer to the current object (which is preloaded into memory) and buffers elements to the left and right of that object. The pointer can be moved either by using next() and prev() or by setCurrent().

Important note -- You should make sure that you keep track of the contents of the folder being used to store these large objects. Although calling clear() deletes all the file once you are done with them, make sure you delete the contents of the folder in other places to ensure that your used disk space doesn't grow out of hand.

Below is an example as to how you might use the BitmapList class to create a gallery type activity, where pressing the
next button moves on to the next picture, but the number of pictures is so large they cannot all be stored in memory.

    // This code would generate the list
    BitmapList list = new BitmapList(DIRECTORY);

    for (int i = 0; i < 200; i ++) {
        Bitmap b = generateBitmap()
        list.add(new BitmapListItem(b));
    }

    .
    .
    .

    // This is done because the list is less like an array with random access, and more like a
    // doubly linked list in that it is fastest to access adjacent elements.
    // This loads the elements at 0 and at 1 into memory
    list.setCurrent(0);

    // Getting elements from the list must be done like this becuase they might have to be loaded
    // in from a file, so cannot be guaranteed to complete on time.

    // In my experience, this usually fires very quickly. (More often than not, the element is already
    // completely buffered, so it fires immediately).
    list.get(new OnListCompleted<Bitmap>() {
        public void onLoadCompleted(Bitmap b) {

            // The bitmap can be used here. Note that this is not on the UI thread, so you may want to use
            // a handler for certain tasks.
            imageView.setImageBitmap(b)
        }
    }

    Button next = ...
    next.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            // This moves the pointer on one
            // It also loads element 2 into memory (and, if -1 were a valid element, it would be stored back
            // into its file).
            list.next();

            // Once again, this will probably fire very quickly
            // as the buffer means that this element will have already
            // been loaded
            list.get(new OnListCompleted<Bitmap> () {
                public void onLoadCompleted(Bitmap b) {
                    imageView.setImageBitmap(b);
                }
            }
        }
    }

It is also possible to use this for any large items you want stored in files when they aren't in memory.
To do this, you need to define your own FileListItem:

    class LargeItem extends FileListItem<LargeClass> {
        public LargeItem(LargeClass item) {
            super(item);
        }

        // These methods must be implemented to load and save the file.
        // For an implemented example, see the BitmapListItem class.
        @Override
        public void write(LargeClass item, File to) {
            // Here, you have to write some implementation that writes item to the file.
            // This is the file that will be passed in get (below).
        }

        @Override
        public LargeClass get(File from) {
            // This should have an implementation to remove the item from the file and return it

        }
    }