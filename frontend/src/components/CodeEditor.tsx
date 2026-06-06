import Editor, { type OnMount } from '@monaco-editor/react';
import { useRef } from 'react';

interface Props {
  value: string;
  onChange: (value: string) => void;
  onSubmit?: () => void;
  onSave?: () => void;
  readOnly?: boolean;
  height?: string;
}

/**
 * Monaco editor configured for Java with the dark theme. Ctrl+Enter triggers
 * submit; Ctrl+S triggers an explicit snapshot save.
 */
export default function CodeEditor({ value, onChange, onSubmit, onSave, readOnly, height }: Props) {
  const submitRef = useRef(onSubmit);
  submitRef.current = onSubmit;
  const saveRef = useRef(onSave);
  saveRef.current = onSave;

  const handleMount: OnMount = (editor, monaco) => {
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => submitRef.current?.());
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => saveRef.current?.());
  };

  return (
    <Editor
      height={height ?? '100%'}
      language="java"
      theme="vs-dark"
      value={value}
      onChange={(v) => onChange(v ?? '')}
      onMount={handleMount}
      options={{
        fontSize: 14,
        fontFamily: "'JetBrains Mono', monospace",
        minimap: { enabled: false },
        scrollBeyondLastLine: false,
        automaticLayout: true,
        tabSize: 4,
        wordWrap: 'on',
        lineNumbers: 'on',
        renderLineHighlight: 'all',
        suggestOnTriggerCharacters: true,
        readOnly: Boolean(readOnly),
        padding: { top: 12 },
      }}
    />
  );
}
