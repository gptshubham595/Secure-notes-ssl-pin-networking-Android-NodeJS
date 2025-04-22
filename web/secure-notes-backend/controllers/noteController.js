const Note = require('../models/Note');

// Get all notes for a user
exports.getNotes = async (req, res) => {
  try {
    const notes = await Note.find({ user: req.user.id }).sort({ updatedAt: -1 });
    res.json(notes);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
};

// Get a specific note
exports.getNote = async (req, res) => {
  try {
    const note = await Note.findById(req.params.id);
    
    // Check if note exists
    if (!note) {
        return res.status(404).json({ msg: 'Note not found' });
    }
    
    // Make sure user owns the note
    if (note.user.toString() !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized' });
    }
    
    res.json(note);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
};

// Create a note
exports.createNote = async (req, res) => {
  const { title, content } = req.body;
  
  try {
    const newNote = new Note({
      title,
      content,
      user: req.user.id
    });
    
    const note = await newNote.save();
    res.json(note);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
};

// Update a note
exports.updateNote = async (req, res) => {
  const { title, content } = req.body;
  
  // Build note object
  const noteFields = {};
  if (title) noteFields.title = title;
  if (content) noteFields.content = content;
  noteFields.updatedAt = Date.now();
  
  try {
    let note = await Note.findById(req.params.id);
    
    // Check if note exists
    if (!note) {
      return res.status(404).json({ msg: 'Note not found' });
    }
    
    // Make sure user owns the note
    if (note.user.toString() !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized' });
    }
    
    note = await Note.findByIdAndUpdate(
      req.params.id,
      { $set: noteFields },
      { new: true }
    );
    
    res.json(note);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
};

// Delete a note
exports.deleteNote = async (req, res) => {
  try {
    let note = await Note.findById(req.params.id);
    
    // Check if note exists
    if (!note) {
      return res.status(404).json({ msg: 'Note not found' });
    }
    
    // Make sure user owns the note
    if (note.user.toString() !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized' });
    }
    
    await Note.findByIdAndRemove(req.params.id);
    
    res.json({ msg: 'Note removed' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
};