<?php
namespace Foo\Bar {
    /**
     * @Annotation()
     * @Target({"CLASS"})
     */
    class Note1 {
        public $value;
    }

    /**
     * @Annotation
     */
    class Note2 {
        public $variable;
    }

    /**
     * @Annotation()
     * @Target("METHOD")
     */
    class NoteForMethod {
    }
    class Note3 {
    }
    trait Note4 {
    }
}

namespace Another\Foo\Bar {
    /**
     * @Annotation
     */
    class AnotherNote1 {
    }

    /**
     * @Annotation
     */
    class AnotherNote2 {
    }
}
