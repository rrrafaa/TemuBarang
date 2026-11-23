begin;

create function public.require_recovery_evidence() returns trigger
language plpgsql set search_path = '' as $$
begin
    if nullif(btrim(new.evidence_path), '') is null then
        raise exception 'Foto kondisi barang wajib disertakan' using errcode = '23514';
    end if;
    return new;
end;
$$;

create trigger temubarang_require_recovery_evidence
before insert on public.recovery_requests
for each row execute function public.require_recovery_evidence();

commit;
